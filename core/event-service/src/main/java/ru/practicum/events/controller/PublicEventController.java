package ru.practicum.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.SearchEventsParams;
import ru.practicum.events.service.PublicEventsService;
import ru.practicum.events.validation.SearchParamsValidator;

import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicEventController {

    private final PublicEventsService publicEventsService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>>
    getFilteredEvents(@RequestParam(required = false, defaultValue = "") String text,
                      @RequestParam(required = false, defaultValue = "") List<Long> categories,
                      @RequestParam(required = false) Boolean paid,
                      @RequestParam(required = false) String rangeStart,
                      @RequestParam(required = false) String rangeEnd,
                      @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                      @RequestParam(required = false, defaultValue = "EVENT_DATE") String sort,
                      @RequestParam(required = false, defaultValue = "0") int from,
                      @RequestParam(required = false, defaultValue = "10") int size) {
        SearchEventsParams searchEventsParams =
                new SearchEventsParams(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        log.info("\nPublicEventController.getFilteredEvents {}", searchEventsParams);
        SearchParamsValidator.validateSearchParams(searchEventsParams);
        List<EventShortDto> result = publicEventsService.getFilteredEvents(searchEventsParams);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> getEventInfo(@PathVariable
                                                     @Min(value = 1, message = "ID must be positive") Long id,
                                                     HttpServletRequest request,
                                                     @RequestHeader("X-EWM-USER-ID") long userId) {

        EventFullDto eventFullDto = publicEventsService.getEventInfo(id, userId);
        return ResponseEntity.status(HttpStatus.OK).body(eventFullDto);
    }

    @PutMapping("/{eventId}/like")
    public void likeEvent(@RequestHeader("X-EWM-USER-ID") long userId,
                          @PathVariable("eventId")
                          @Min(value = 1, message = "ID must be positive") Long id) throws IllegalAccessException {
        publicEventsService.likeEvent(id, userId);
    }

    @GetMapping("/recommendations")
    public List<EventShortDto> getRecommendationsForUser(@RequestHeader("X-EWM-USER-ID") long userId, long maxResults) {
        return publicEventsService.getRecommendationsForUser(userId, maxResults);
    }

    @GetMapping("/{eventId}/recommendations/")
    public List<EventShortDto> getSimilarEvents(@RequestHeader("X-EWM-USER-ID") long userId,
                                                @PathVariable("eventId") long eventId,
                                                long maxResults) {
        return publicEventsService.getSimilarEvents(userId, eventId, maxResults);
    }
}