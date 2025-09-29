package ru.practicum.events.service;

import jakarta.validation.constraints.Min;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.SearchEventsParams;
import ru.practicum.events.model.Event;
import ru.practicum.exception.ExternalServiceException;

import java.util.List;

public interface PublicEventsService {

    Event getEvent(Long id);

    Double getEventRating(long id) throws ExternalServiceException;

    EventFullDto getEventInfo(Long id, Long userId) throws ExternalServiceException;

    List<EventShortDto> getFilteredEvents(SearchEventsParams searchEventsParams) throws ExternalServiceException;

    EventFullDto getEventAnyStatusWithViews(Long id) throws ExternalServiceException;

    void likeEvent(@Min(value = 1, message = "ID must be positive") Long id, long userId) throws IllegalAccessException;

    List<EventShortDto> getRecommendationsForUser(long userId, long maxResults) throws ExternalServiceException;
}
