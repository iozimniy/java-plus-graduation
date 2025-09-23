package ru.practicum.events.service;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.user.dto.EventRequestStatusUpdateRequest;
import ru.practicum.user.dto.EventRequestStatusUpdateResult;
import ru.practicum.user.dto.GetUserEventsDto;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface PrivateUserEventService {
    List<EventShortDto> getUsersEvents(GetUserEventsDto dto);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto addNewEvent(UserDto userDto, NewEventDto eventDto);

    EventFullDto updateUserEvent(UserDto userDto, Long eventId, UpdateEventUserRequest updateDto);

    List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateUserEventRequest(UserDto userDto, Long eventId, EventRequestStatusUpdateRequest request);
}
