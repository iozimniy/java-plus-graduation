package ru.practicum.user.errors;

import org.springframework.dao.DataIntegrityViolationException;

public class NotPublishedEventParticipationException extends DataIntegrityViolationException {
    public NotPublishedEventParticipationException(String message) {
        super(message);
    }
}
