package com.starta.project.global.exception.custom;

public class CustomUserBlockedException extends RuntimeException {
    public CustomUserBlockedException(String message) {
        super(message);
    }
}