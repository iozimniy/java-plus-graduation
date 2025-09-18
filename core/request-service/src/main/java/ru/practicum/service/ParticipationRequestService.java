package ru.practicum.service;

import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.ParticipationRequestUpdateStatusDto;

import java.util.List;
import java.util.Map;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getUserRequestsByEventId(Long eventId);

    List<ParticipationRequestDto> getRequestsByIds(List<Long> ids);

    void updateStatusByIds(ParticipationRequestUpdateStatusDto requestUpdateStatusDto);

    int getConfirmedRequests(Long eventId);

    Map<Long, Integer> getConfirmedRequestsForList(List<Long> ids);
}
