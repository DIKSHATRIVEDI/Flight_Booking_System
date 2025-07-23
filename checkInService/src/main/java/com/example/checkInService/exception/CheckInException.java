package com.example.checkInService.exception;

public class CheckInException extends RuntimeException {
    public CheckInException(String message) {
        super(message);
    }

    public CheckInException(String message, Throwable cause) {
        super(message, cause);
    }
}
