package ru.practicum.events.service;

import com.querydsl.core.BooleanBuilder;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.client.CollectorClient;
import ru.practicum.commons.config.DateConfig;
import ru.practicum.commons.errors.EventNotPublishedException;
import ru.practicum.commons.errors.ForbiddenActionException;
import ru.practicum.controller.ClientAdapter;
import ru.practicum.dto.ReadEndpointHitDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.SearchEventsParams;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.QEvent;
import ru.practicum.event.constants.StateEvent;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.request.client.ParticipationRequestClient;
import ru.practicum.request.constants.ParticipationRequestStatus;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventsServiceImpl implements PublicEventsService {

    private final EventRepository eventRepository;

    //private final ClientAdapter clientAdapter;
    private final CollectorClient collectorClient;

    private final ParticipationRequestClient requestClient;

    @Override
    public Event getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=" + id + " was not found"));

        Integer confirmedCount = null;

        try {
            confirmedCount = requestClient.getConfirmedRequestsCount(id);
        } catch (FeignException e) {
            log.error("ParticipationRequestClient error: getConfirmedRequestsCount(eventId) with {}", e);
        }

        event.setConfirmedRequests(confirmedCount);

        return event;
    }

    // TODO: тут получаем хиты, а нужно получать рейтинг
    @Override
    public int getEventsViews(long id, LocalDateTime publishedOn) {
        List<String> uris = List.of("/events/" + id);
        List<ReadEndpointHitDto> res = clientAdapter.getHits(publishedOn.format(DateConfig.FORMATTER),
                LocalDateTime.now().format(DateConfig.FORMATTER), uris, true);
        log.info("\nPublicEventsServiceImpl.getEventsViews: res {}", res);
        return (CollectionUtils.isEmpty(res)) ? 0 : res.getFirst().getHits();
    }

    @Override
    public EventFullDto getEventAnyStatusWithViews(Long id) {
        //Attention: this method works without saving views!

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=" + id + " was not found"));

        Integer confirmedCount = null;

        try {
            confirmedCount = requestClient.getConfirmedRequestsCount(id);
        } catch (FeignException e) {
            log.error("ParticipationRequestClient error: getConfirmedRequestsCount(eventId) with {}", e);
        }

        event.setConfirmedRequests(confirmedCount);

        if (!event.getState().equals(StateEvent.PUBLISHED)) {
            throw new EventNotPublishedException("There is no published event id " + event.getId());
        }

        //TODO: тут сетим просмотры, заменить на рейтинг
        event.setViews(getEventsViews(event.getId(), event.getPublishedOn()));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public void likeEvent(Long id, long userId) throws IllegalAccessException {
        Event event = eventRepository.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Event with id=" + id + " was not found"));

        //проверка, что мероприятие уже завершено
        if (event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new IllegalAccessException("Event date is in the future");
        }

        //получаем заявку
        ParticipationRequestDto requestDto = null;

        try {
            requestDto = requestClient.getRequest(userId, id);
        } catch (EntityNotFoundException e) {
            throw new IllegalAccessException("Request for event not found");
        } catch (Exception e) {
            log.error("Request for get ParticipationRequest with userId {} and eventId {} failed", userId, id);
        }

        //проверяем статус заявки
        if (!requestDto.getStatus().equals(ParticipationRequestStatus.CONFIRMED)) {
            throw new IllegalAccessException("Request status is not confirmed");
        }

        //сохраняем лайк
        try {
            collectorClient.sendUserAction(userId, id, ActionTypeProto.ACTION_LIKE, Instant.now());
        } catch (Exception e) {
            log.error("Request for like event with eventId {} by user with userId {} failed", id, userId);
        }
    }

    public List<Event> getEventsByListIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids))
            return List.of();

        List<Event> events = eventRepository.findEvents(ids);

        events = addConfirmedCounts(events);

        if (CollectionUtils.isEmpty(events))
            return events;

        LocalDateTime start = events.stream()
                .map(Event::getPublishedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() ->
                        new RuntimeException("Internal server error during execution PublicEventsServiceImpl"));
        List<String> uris = events.stream()
                .map(event -> "/event/" + event.getId())
                .toList();
        // TODO: тут получаем просмотры, а нужно получать рейтинг
        List<ReadEndpointHitDto> acceptedList = clientAdapter.getHits(start.format(DateConfig.FORMATTER),
                LocalDateTime.now().format(DateConfig.FORMATTER), uris, true);
        // Заносим значения views в список events
        viewsToEvents(acceptedList, events);
        return events;
    }

    @Override
    public EventFullDto getEventInfo(Long id, Long userId) {
        log.info("\nPublicEventsServiceImpl.getEventInfo: accepted {}", id);
        Event event = getEvent(id);
        log.info("\nPublicEventsServiceImpl.getEventsViews: event {}", event);
        if (!event.getState().equals(StateEvent.PUBLISHED)) {
            throw new EventNotPublishedException("There is no published event id " + event.getId());
        }

        // TODO: тут сетим просмотры, а нужно сетить рейтинг
        // Получаем views
        event.setViews(getEventsViews(event.getId(), event.getPublishedOn()));

        //Имеем новый просмотр - сохраняем его
        collectorClient.sendUserAction(userId, id, ActionTypeProto.ACTION_VIEW, Instant.now());

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getFilteredEvents(SearchEventsParams searchEventsParams) {
        log.info("\nPublicEventsServiceImpl.getFilteredEvents: {}", searchEventsParams);

        BooleanBuilder builder = new BooleanBuilder();

        // Добавляем условия отбора по контексту
        if (!Strings.isEmpty(searchEventsParams.getText())) {
            builder.or(QEvent.event.annotation.containsIgnoreCase(searchEventsParams.getText()))
                    .or(QEvent.event.description.containsIgnoreCase(searchEventsParams.getText()));
        }

        // Добавляем отбор по статусу PUBLISHED
        builder.and(QEvent.event.state.eq(StateEvent.PUBLISHED));
        // ... и по списку категорий
        if (!CollectionUtils.isEmpty(searchEventsParams.getCategories()))
            builder.and(QEvent.event.category.id.in(searchEventsParams.getCategories()));

        // ... и еще по признаку платные/бесплатные
        if (searchEventsParams.getPaid() != null)
            builder.and(QEvent.event.paid.eq(searchEventsParams.getPaid()));

        // Добавляем условие диапазона дат
        LocalDateTime start;
        LocalDateTime end;
        if (searchEventsParams.getRangeStart() == null) {
            start = LocalDateTime.now();
            searchEventsParams.setRangeStart(start.format(DateConfig.FORMATTER));
        } else {
            start = LocalDateTime.parse(searchEventsParams.getRangeStart(), DateConfig.FORMATTER);
        }
        if (searchEventsParams.getRangeEnd() == null) {
            builder.and(QEvent.event.eventDate.goe(start));
        } else {
            end = LocalDateTime.parse(searchEventsParams.getRangeEnd(), DateConfig.FORMATTER);
            builder.and(QEvent.event.eventDate.between(start, end));
        }

        List<Event> events = eventRepository.searchEvents(builder);

        events = addConfirmedCounts(events);

        if (searchEventsParams.getOnlyAvailable()) {
            events.stream()
                    .filter(ev -> ev.getParticipantLimit() == 0 ||
                            ev.getParticipantLimit() > ev.getConfirmedRequests())
                    .toList();
        }

        int toIndex = Math.min(searchEventsParams.getFrom() + searchEventsParams.getSize(), events.size());
        if (searchEventsParams.getFrom() >= events.size()) {
            return List.of();
        }
        events = events.subList(searchEventsParams.getFrom(), toIndex);

        log.info("PublicEventsServiceImpl.getFilteredEvents: events {}", events);
        // Если не было установлено rangeEnd, устанавливаем
        if (searchEventsParams.getRangeEnd() == null) {
            searchEventsParams.setRangeEnd(LocalDateTime.now().format(DateConfig.FORMATTER));
        }
        // Формируем список uris
        List<String> uris = new ArrayList<>();
        for (Event e : events) {
            uris.add("/events/" + e.getId());
        }

        // TODO: тут получаем рейтинг (вместо просмотров) мероприятия, нужно заменить, когда будет реализован
        List<ReadEndpointHitDto> acceptedList = clientAdapter.getHits(searchEventsParams.getRangeStart(),
                searchEventsParams.getRangeEnd(), uris, true);
        viewsToEvents(acceptedList, events);

        // Сортировка. Для начала проверяем значение параметра сортировки
        String sortParam;
        if (Strings.isEmpty(searchEventsParams.getSort())) {
            sortParam = "VIEWS";
        } else {
            sortParam = searchEventsParams.getSort().toUpperCase();
        }
        // Дополняем сортировкой
        List<Event> sortedEvents = new ArrayList<>();
        if (sortParam.equalsIgnoreCase("EVENT_DATE")) {
            sortedEvents = events.stream()
                    .sorted(Comparator.comparing(Event::getEventDate)) // Сортируем по eventDate
                    .toList();
        } else {
            // TODO: должны сортировать по рейтингу
            sortedEvents = events.stream()
                    .sorted(Comparator.comparingInt(Event::getViews).reversed()) // Сортируем по views
                    .toList();
        }

        log.info("\n Final list {}", sortedEvents);
        return EventMapper.toListEventShortDto(sortedEvents);
    }

    // TODO: заменить views на rating
    // тут мы сетим рейтинг мероприятия. Когда функционал рейтинга будует реализован,
    // нужно переписать этот метод
    public void viewsToEvents(List<ReadEndpointHitDto> viewsList, List<Event> events) {
        // Заносим значения views в список events
        Map<Integer, Integer> workMap = new HashMap<>();
        for (ReadEndpointHitDto r : viewsList) {
            int i = Integer.parseInt(r.getUri().substring(r.getUri().lastIndexOf("/") + 1));
            workMap.put(i, r.getHits());
        }
        for (Event e : events) {
            e.setViews(workMap.getOrDefault(e.getId(), 0));
        }
    }

    private List<Event> addConfirmedCounts(List<Event> events) {

        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Integer> eventsConfirmedCounts = requestClient.getConfirmedRequestsCountForList(ids);

        for (Event foundEvent : events) {
            foundEvent.setConfirmedRequests(eventsConfirmedCounts.getOrDefault(foundEvent.getId(), 0));
        }

        return events;
    }
}