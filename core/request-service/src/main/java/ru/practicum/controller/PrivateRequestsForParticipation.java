package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.client.EventClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.service.ParticipationRequestService;
import ru.practicum.user.client.UserClient;
import ru.practicum.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@Slf4j
@RequiredArgsConstructor
public class PrivateRequestsForParticipation {

    private final ParticipationRequestService participationRequestService;

    private final EventClient eventClient;

    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable Long userId) {
        log.info("Request to get participation requests for user {}", userId);
        List<ParticipationRequestDto> participationRequests = participationRequestService.getUserRequests(userId);
        return ResponseEntity.ok(participationRequests);
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> addParticipationRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {
        log.info("Request to add participation request for user {} for event {}", userId, eventId);

        UserDto userDto = null;

        try {
            userDto = userClient.getUser(userId);
        } catch (Exception e) {
            log.error("Request for get user with id {} to userClient is failed with message {}", userId, e);
        }

        EventFullDto event = null;

        try {
            event = eventClient.getEventById(eventId);
            log.debug("Event from EventClient: {}", event);
        } catch (Exception e) {
            log.error("Request for get event with id {} to eventClient is failed with message {}", eventId, e);
        }

        ParticipationRequestDto participationRequest =
                participationRequestService.addParticipationRequest(userDto, eventId, event);
        return ResponseEntity.status(HttpStatus.CREATED).body(participationRequest);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {
        log.info("Request to cancel participation request for user {} and request {}", userId, requestId);
        ParticipationRequestDto cancelledRequest = participationRequestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(cancelledRequest);
    }
}