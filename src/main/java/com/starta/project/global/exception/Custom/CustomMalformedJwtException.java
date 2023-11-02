package com.starta.project.global.exception.Custom;

public class CustomMalformedJwtException extends RuntimeException {
    public CustomMalformedJwtException(String message) {
        super(message);
    }
}