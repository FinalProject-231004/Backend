package com.starta.project.global.exception.custom;

public class CustomUnsupportedJwtException extends RuntimeException {
    public CustomUnsupportedJwtException(String message) {
        super(message);
    }
}