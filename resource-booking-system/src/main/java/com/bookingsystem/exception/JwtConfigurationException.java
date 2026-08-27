package com.bookingsystem.exception;

public class JwtConfigurationException extends IllegalStateException {

    public JwtConfigurationException(String message) {
        super(message);
    }

    public JwtConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}