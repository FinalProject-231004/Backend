package com.starta.project.global.exception.Custom;


public class CustomExpiredJwtException extends RuntimeException {
    public CustomExpiredJwtException(String message) {
        super(message);
    }
}

