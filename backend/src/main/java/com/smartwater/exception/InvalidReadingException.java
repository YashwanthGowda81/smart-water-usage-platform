package com.smartwater.exception;

public class InvalidReadingException extends RuntimeException {
    public InvalidReadingException(String message) {
        super(message);
    }
}
