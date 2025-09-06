package ru.practicum.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.request.constants.ParticipationRequestStatus;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service", path = "/users/{userId}/requests")
public interface ParticipationRequestClient {

    @GetMapping("/{eventId}")
    List<ParticipationRequestDto> getRequestsByEventId(@PathVariable Long userId,
                                                              @PathVariable Long eventId);

    @GetMapping
    List<ParticipationRequestDto> getRequestsByIds(List<Long> ids);

    @PatchMapping
    void updateStatusByIds(ParticipationRequestStatus status, List<Long> ids);
}
