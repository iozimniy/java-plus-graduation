package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import ru.practicum.event.dto.EventFullDto;

@FeignClient(name = "event-service", path = "/")
public interface EventClient {

    @GetMapping("/{eventId}")
    EventFullDto getEventById(Long id);
}
