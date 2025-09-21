package ru.practicum.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.events.service.PrivateUserEventService;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.user.client.UserClient;
import ru.practicum.user.dto.EventRequestStatusUpdateRequest;
import ru.practicum.user.dto.EventRequestStatusUpdateResult;
import ru.practicum.user.dto.GetUserEventsDto;
import ru.practicum.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@Validated
@Slf4j
@RequiredArgsConstructor
public class PrivateUserEventController {
    private final PrivateUserEventService privateUserService;

    private final UserClient userClient;


    @GetMapping
    public ResponseEntity<List<EventShortDto>> getUserEvents(@PathVariable("userId") Long userId,
                                                             @RequestParam(required = false, defaultValue = "0") int from,
                                                             @RequestParam(required = false, defaultValue = "10") int size) {
        log.info("\nRequest getting user {} events", userId);
        GetUserEventsDto dto = new GetUserEventsDto(userId, from, size);
        return ResponseEntity.status(HttpStatus.OK).body(privateUserService.getUsersEvents(dto));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getUserEventById(@PathVariable("userId") Long userId,
                                                         @PathVariable("eventId") Long eventId) {
        log.info("\nRequest getting user {} event {}", userId, eventId);
        return ResponseEntity.status(HttpStatus.OK).body(privateUserService.getUserEventById(userId, eventId));
    }

    @PostMapping
    public ResponseEntity<EventFullDto> addNewEvent(@PathVariable("userId") Long userId,
                                                    @Valid @RequestBody NewEventDto eventDto) {
        log.info("\nRequest for adding new event {}", eventDto);

        UserDto user = getUser(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(privateUserService.addNewEvent(user, eventDto));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateUserEvent(@PathVariable("userId") Long userId,
                                                        @PathVariable("eventId") Long eventId,
                                                        @Valid @RequestBody UpdateEventUserRequest updateDto) {
        log.info("\nRequest for updating existing event {}", updateDto);

        UserDto userDto = getUser(userId);

        return ResponseEntity.status(HttpStatus.OK).body(privateUserService.updateUserEvent(userDto, eventId, updateDto));
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getUserEventRequests(@PathVariable("userId") Long userId,
                                                                              @PathVariable("eventId") Long eventId) {
        log.info("\nRequest getting user {} event {} requests", userId, eventId);
        return ResponseEntity.status(HttpStatus.OK).body(privateUserService.getUserEventRequests(userId, eventId));
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateUserEventRequestStatus(@PathVariable("userId") Long userId,
                                                                                       @PathVariable("eventId") Long eventId,
                                                                                       @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("RequestIds: {}, Status: {}", request.getRequestIds(), request.getStatus());

        UserDto userDto = getUser(userId);

        return ResponseEntity.status(HttpStatus.OK).body(privateUserService.updateUserEventRequest(userDto, eventId, request));
    }

    private UserDto getUser(Long userId) {

        UserDto user = null;

        try {
            user = userClient.getUser(userId);
        } catch (Exception e) {
            log.error("Request for get user with id {} to userClient is failed with message {}", userId, e);
        }

        return user;
    }

}
