package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.client.ParticipationRequestClient;
import ru.practicum.request.constants.ParticipationRequestStatus;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.service.ParticipationRequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/requests")
@Slf4j
@RequiredArgsConstructor
public class InternalParticipationRequestController implements ParticipationRequestClient {

    private final ParticipationRequestService participationRequestService;

    //тут методы для межсервисного взаимодействия

    @GetMapping("/{eventId}")
    public List<ParticipationRequestDto> getRequestsByEventId(@PathVariable Long userId,
                                                              @PathVariable Long eventId) {
        return participationRequestService.getUserRequestsByEventId(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> getRequestsByIds(@RequestBody List<Long> ids) {
        return participationRequestService.getRequestsByIds(ids);
    }

    @PatchMapping
    public void updateStatusByIds(ParticipationRequestStatus status, List<Long> ids) {
        participationRequestService.updateStatusByIds(status, ids);
    }

    @GetMapping("/count/confirmed/{eventId}")
    public int getConfirmedRequestsCount(@PathVariable Long eventId) {
        return participationRequestService.getConfirmedRequests(eventId);
    }

    @GetMapping("/count/confirmed")
    public Map<Long, Integer> getConfirmedRequestsCountForList(@RequestBody List<Long> ids) {
        return participationRequestService.getConfirmedRequestsForList(ids);
    }
}
