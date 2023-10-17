package com.starta.project.global.exception.Custom;

/**
 * [JWT의 형식이 올바르지 않을 때 발생하는 예외]
 * JWT는 특정한 형식 (헤더, 페이로드, 서명)을 가집니다.
 * 만약 제공된 토큰이 이 형식을 따르지 않으면 (예: 일부 부분이 누락된 경우) 이 예외가 발생합니다.
 */

public class CustomUnsupportedJwtException extends RuntimeException {
    public CustomUnsupportedJwtException(String message) {
        super(message);
    }
}