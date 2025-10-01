package ru.practicum.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.event.constants.StateEvent;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.errors.EventOwnerParticipationException;
import ru.practicum.user.errors.EventParticipationLimitException;
import ru.practicum.user.errors.NotPublishedEventParticipationException;
import ru.practicum.user.errors.RepeatParticipationRequestException;

@Component
@RequiredArgsConstructor
public class ParticipationRequestValidator {

    private final ParticipationRequestRepository requestRepository;

    public RuntimeException checkRequest(UserDto user, EventFullDto event, long confirmedRequestsCount) {
        if (event.getInitiator().getId().equals(user.getId())) {
            return new EventOwnerParticipationException("Event initiator cannot participate in their own event");
        }

        if (event.getState() != StateEvent.PUBLISHED) {
            return new NotPublishedEventParticipationException("Cannot participate in an unpublished event");
        }

        if (requestRepository.existsByUserIdAndEventId(user.getId(), event.getId())) {
            return new RepeatParticipationRequestException("User already has a participation request for this event");
        }

        if (event.getParticipantLimit() > 0 && confirmedRequestsCount >= event.getParticipantLimit()) {
            return new EventParticipationLimitException("Event participant limit reached");
        }

        return null;
    }
}