package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.client.ParticipationRequestClient;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.ParticipationRequestUpdateStatusDto;
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
    public List<ParticipationRequestDto> getRequestsByEventId(@PathVariable("eventId") Long eventId) {
        return participationRequestService.getUserRequestsByEventId(eventId);
    }

    @PostMapping
    public List<ParticipationRequestDto> getRequestsByIds(@RequestBody List<Long> ids) {
        return participationRequestService.getRequestsByIds(ids);
    }

    @PutMapping
    public void updateStatusByIds(@RequestBody ParticipationRequestUpdateStatusDto requestUpdateStatusDto) {
        participationRequestService.updateStatusByIds(requestUpdateStatusDto);
    }

    @GetMapping("/count/confirmed/{eventId}")
    public Integer getConfirmedRequestsCount(@PathVariable("eventId") Long eventId) {
        return participationRequestService.getConfirmedRequests(eventId);
    }

    @PostMapping("/count/confirmed/list")
    public Map<Long, Integer> getConfirmedRequestsCountForList(@RequestBody List<Long> ids) {
        return participationRequestService.getConfirmedRequestsForList(ids);
    }
}
