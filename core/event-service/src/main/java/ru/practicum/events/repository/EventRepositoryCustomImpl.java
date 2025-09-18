package ru.practicum.events.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import feign.FeignException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.QEvent;
import ru.practicum.request.client.ParticipationRequestClient;
import ru.practicum.request.constants.ParticipationRequestStatus;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventRepositoryCustomImpl implements EventRepositoryCustom {
    private final EntityManager em;
    private final ParticipationRequestClient requestClient;

    @Override
    public Page<Event> findAllWithBuilder(BooleanBuilder builder, Pageable pageable) {
        QEvent event = QEvent.event;

        // Запрос для получения данных с пагинацией и сортировкой
        JPAQuery<Event> query = new JPAQuery<>(em)
                .select(event)
                .from(event)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        List<Event> content = query.fetch();

        // Запрос для подсчета общего количества элементов (используем count)
        Long total = new JPAQuery<>(em).select(event.count())
                .from(event)
                .fetchOne();

        if (total == null)
            throw new RuntimeException("Не удалось определить количество событий");

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Event findEventWithStatus(Long eventId, ParticipationRequestStatus status) {
        QEvent event = QEvent.event;

        Event foundEvent = new JPAQuery<>(em)
                .select(event)
                .from(event)
                .where(event.id.eq(eventId))
                .fetchOne();

        if (foundEvent == null) {
            throw new EntityNotFoundException("Event with" + eventId + " not found");
        }

        int confirmedCount = 0;

        if (status.equals(ParticipationRequestStatus.CONFIRMED)) {
            try {
                confirmedCount = requestClient.getConfirmedRequestsCount(eventId);
            } catch (FeignException e) {
                log.error("ParticipationRequestClient error: getConfirmedRequestsCount(eventId) with {}", e);
            }
        }

        foundEvent.setConfirmedRequests(confirmedCount);
        return foundEvent;
    }

    @Override
    public List<Event> searchEvents(BooleanBuilder eventCondition, ParticipationRequestStatus status,
                                    boolean onlyAvailable, int from, int size) {
        QEvent event = QEvent.event;
        List<Event> events = new JPAQuery<>(em)
                .select(event)
                .from(event)
                .where(eventCondition)
                .fetch();

        if (events.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        events = addConfirmedCounts(events);


        if (onlyAvailable) {
            events.stream()
                    .filter(ev -> ev.getParticipantLimit() == 0 ||
                            ev.getParticipantLimit() > ev.getConfirmedRequests())
                    .toList();
        }

        int toIndex = Math.min(from + size, events.size());
        if (from >= events.size()) {
            return List.of();
        }
        return events.subList(from, toIndex);
    }

    @Override
    public List<Event> findEventsWithConfirmedCount(List<Long> eventIds) {
        QEvent event = QEvent.event;

        List<Event> events = new JPAQuery<>(em)
                .select(event)
                .from(event)
                .where(event.id.in(eventIds))
                .fetch();

        if (events.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        events = addConfirmedCounts(events);



        return events;
    }

    @Override
    public Event getSingleEvent(Long id) {
        QEvent event = QEvent.event;

        Event eventResult = new JPAQuery<>(em)
                .select(event)
                .from(event)
                .where(event.id.eq(id))
                .fetchOne();

        if (eventResult == null) {
            return null;
        }

        int confirmedCount = 0;

        try {
            confirmedCount = requestClient.getConfirmedRequestsCount(id);
        } catch (FeignException e) {
            log.error("ParticipationRequestClient error: getConfirmedRequestsCount(eventId) with {}", e);
        }

        return eventResult;
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
