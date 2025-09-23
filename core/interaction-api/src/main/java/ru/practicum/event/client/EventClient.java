package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;

@FeignClient(name = "event-service", path = "/internal/events")
public interface EventClient {

    @GetMapping("/{eventId}")
    EventFullDto getEventById(@PathVariable("eventId") Long id);

    @GetMapping("/short/{eventId}")
    EventShortDto getShortEvent(@PathVariable("eventId") Long id);

    @GetMapping("/views/{eventId}")
    EventFullDto getEventAnyStatusWithViews(@PathVariable("eventId") Long id);
}
