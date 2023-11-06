package com.starta.project.global.exception.custom;

public class CustomMalformedJwtException extends RuntimeException {
    public CustomMalformedJwtException(String message) {
        super(message);
    }
}