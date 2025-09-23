package ru.practicum.events.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.client.EventClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.events.service.AdminEventService;
import ru.practicum.events.service.PublicEventsService;

@RestController
@RequestMapping("/internal/events")
@Slf4j
@RequiredArgsConstructor
public class InternalEventController implements EventClient {

    private final AdminEventService adminEventService;

    private final PublicEventsService publicEventsService;

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable("eventId") Long id) {
        log.info("Request for event by id {}", id);
        return adminEventService.getEventById(id);
    }

    @GetMapping("/short/{eventId}")
    public EventShortDto getShortEvent(@PathVariable("eventId") Long id) {
        log.info("Request for EventShortDto with id {}", id);
        return adminEventService.getEventShortDto(id);
    }

    @GetMapping("/views/{eventId}")
    public EventFullDto getEventAnyStatusWithViews(@PathVariable("eventId") Long id) {
        return publicEventsService.getEventAnyStatusWithViews(id);
    }
}
