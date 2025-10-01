package ru.practicum.events.service;

import jakarta.validation.constraints.Min;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.SearchEventsParams;
import ru.practicum.events.model.Event;

import java.util.List;

public interface PublicEventsService {

    Event getEvent(Long id);

    Double getEventRating(long id);

    EventFullDto getEventInfo(Long id, Long userId);

    List<EventShortDto> getFilteredEvents(SearchEventsParams searchEventsParams);

    EventFullDto getEventAnyStatusWithViews(Long id);

    void likeEvent(@Min(value = 1, message = "ID must be positive") Long id, long userId) throws IllegalAccessException;

    List<EventShortDto> getRecommendationsForUser(long userId, long maxResults);

    List<EventShortDto> getSimilarEvents(long userId, long eventId, long maxResults);
}
