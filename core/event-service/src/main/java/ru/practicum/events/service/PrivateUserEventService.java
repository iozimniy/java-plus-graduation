package ru.practicum.events.service;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.user.dto.EventRequestStatusUpdateRequest;
import ru.practicum.user.dto.EventRequestStatusUpdateResult;
import ru.practicum.user.dto.GetUserEventsDto;

import java.util.List;

public interface PrivateUserEventService {
    List<EventShortDto> getUsersEvents(GetUserEventsDto dto);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto addNewEvent(Long userId, NewEventDto eventDto);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateDto);

    List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateUserEventRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}
