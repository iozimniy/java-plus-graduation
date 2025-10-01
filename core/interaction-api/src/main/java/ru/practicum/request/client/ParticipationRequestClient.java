package ru.practicum.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.ParticipationRequestUpdateStatusDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", path = "/internal/requests")
public interface ParticipationRequestClient {

    @GetMapping("/{eventId}")
    List<ParticipationRequestDto> getRequestsByEventId(@PathVariable("eventId") Long eventId);

    @PostMapping
    List<ParticipationRequestDto> getRequestsByIds(List<Long> ids);

    @PutMapping
    void updateStatusByIds(@RequestBody ParticipationRequestUpdateStatusDto requestUpdateStatusDto);

    @GetMapping("/count/confirmed/{eventId}")
    Integer getConfirmedRequestsCount(@PathVariable("eventId") Long eventId);

    @PostMapping("/count/confirmed/list")
    Map<Long, Integer> getConfirmedRequestsCountForList(@RequestBody List<Long> ids);

    @GetMapping("{userId}/{eventId}")
    ParticipationRequestDto getRequest(@PathVariable("userId") Long userId,
                                              @PathVariable("eventId") Long eventId);
}
