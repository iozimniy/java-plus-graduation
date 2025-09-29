package ru.practicum.events.repository;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.events.model.Event;

import java.util.List;

public interface EventRepositoryCustom {
    Page<Event> findAllWithBuilder(BooleanBuilder builder, Pageable pageable);

    List<Event> searchEvents(BooleanBuilder eventCondition);

    List<Event> findEvents(List<Long> eventIds);

}