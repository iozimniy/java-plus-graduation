package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.event.dto.EventFullDto;

@FeignClient(name = "event-service", path = "/admin/events")
public interface EventClient {

    @GetMapping("/{eventId}")
    EventFullDto getEventById(@PathVariable("eventId") Long id);
}
