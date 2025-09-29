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
import ru.practicum.client.RecommendationsClient;
import ru.practicum.commons.config.DateConfig;
import ru.practicum.commons.errors.EventNotPublishedException;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.SearchEventsParams;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.QEvent;
import ru.practicum.event.constants.StateEvent;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.request.client.ParticipationRequestClient;
import ru.practicum.request.constants.ParticipationRequestStatus;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventsServiceImpl implements PublicEventsService {

    private final EventRepository eventRepository;

    private final RecommendationsClient recommendationsClient;
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

    @Override
    public Double getEventRating(long id) {
        Stream<RecommendedEventProto> stream = recommendationsClient.getRatings(List.of(id));
        List<Double> ratings = stream.map(proto -> proto.getScore()).toList();
        return ratings.getFirst();
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

        event.setRating(getEventRating(event.getId()));
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

    @Override
    public List<EventShortDto> getRecommendationsForUser(long userId, long maxResults) {
        Stream<RecommendedEventProto> stream = recommendationsClient.getRecommendationsForUser(userId, maxResults);
        List<Long> recommendations = stream.map(proto -> proto.getEventId()).toList();

        List<Event> events = eventRepository.findAllById(recommendations);

        return EventMapper.toListEventShortDto(events);
    }

    @Override
    public List<EventShortDto> getSimilarEvents(long userId, long eventId, long maxResults) {
        Stream<RecommendedEventProto> stream = recommendationsClient.getSimilarEvents(eventId, userId, maxResults);
        List<Long> recommendations = stream.map(proto -> proto.getEventId()).toList();

        List<Event> events = eventRepository.findAllById(recommendations);

        return EventMapper.toListEventShortDto(events);
    }

    public List<Event> getEventsByListIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids))
            return List.of();

        List<Event> events = eventRepository.findEvents(ids);

        events = addConfirmedCounts(events);

        if (CollectionUtils.isEmpty(events)) return events;

        Map<Long, Double> ratings = recommendationsClient.getRatings(ids)
                .collect(Collectors.toMap(
                recommendedEventProto -> recommendedEventProto.getEventId(),
                recommendedEventProto -> recommendedEventProto.getScore()
                ));

        // Заносим значения views в список events
        ratingToEvents(ratings, events);
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

        // Получаем rating
        event.setRating(getEventRating(event.getId()));

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

        //формируем список ids

        List<Long> ids = events.stream().map(event -> event.getId()).toList();

        Map<Long, Double> ratings = recommendationsClient.getRatings(ids)
                        .collect(Collectors.toMap(
                                recommendedEventProto -> recommendedEventProto.getEventId(),
                                recommendedEventProto -> recommendedEventProto.getScore()
                        ));

        ratingToEvents(ratings, events);

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
            sortedEvents = events.stream()
                    .sorted(Comparator.comparingDouble(Event::getRating).reversed()) // Сортируем по views
                    .toList();
        }

        log.info("\n Final list {}", sortedEvents);
        return EventMapper.toListEventShortDto(sortedEvents);
    }

    public void ratingToEvents(Map<Long, Double> ratings, List<Event> events) {
        // Заносим значения ratings в список events
        events.forEach(event -> event.setRating(ratings.getOrDefault(event.getId(), 0.0)));
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