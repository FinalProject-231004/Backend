package com.starta.project.global.exception.Custom;

public class CustomUnsupportedJwtException extends RuntimeException {
    public CustomUnsupportedJwtException(String message) {
        super(message);
    }
}