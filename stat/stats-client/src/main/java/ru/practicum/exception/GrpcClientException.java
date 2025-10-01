package ru.practicum.exception;

public class GrpcClientException extends RuntimeException {
    public GrpcClientException(String message) {
        super(message);
    }
}
