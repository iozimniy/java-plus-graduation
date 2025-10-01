package ru.practicum.service;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.ParticipationRequestUpdateStatusDto;
import ru.practicum.user.dto.UserDto;

import java.util.List;
import java.util.Map;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto addParticipationRequest(UserDto userDto, Long eventId, EventFullDto event);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getUserRequestsByEventId(Long eventId);

    List<ParticipationRequestDto> getRequestsByIds(List<Long> ids);

    void updateStatusByIds(ParticipationRequestUpdateStatusDto requestUpdateStatusDto);

    Integer getConfirmedRequests(Long eventId);

    Map<Long, Integer> getConfirmedRequestsForList(List<Long> ids);

    ParticipationRequestDto getRequest(Long userId, Long eventId);
}
