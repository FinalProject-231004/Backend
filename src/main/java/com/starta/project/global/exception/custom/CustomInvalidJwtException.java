package com.starta.project.global.exception.custom;

public class CustomInvalidJwtException extends RuntimeException {
    public CustomInvalidJwtException(String message) {
        super(message);
    }
}
