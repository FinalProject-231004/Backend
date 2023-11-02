package com.starta.project.global.exception.Custom;

public class CustomInvalidJwtException extends RuntimeException {
    public CustomInvalidJwtException(String message) {
        super(message);
    }
}
