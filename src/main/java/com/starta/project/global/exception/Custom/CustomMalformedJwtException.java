package com.starta.project.global.exception.Custom;

/**
 * [JWT가 예상한 형식 또는 알고리즘이 아닐 때 발생하는 예외]
 * JWT는 여러 알고리즘을 지원하며, 서버는 특정 알고리즘만 지원할 수 있습니다.
 * 클라이언트가 지원되지 않는 알고리즘의 JWT를 제공할 때 이 예외가 발생합니다.
 */

public class CustomMalformedJwtException extends RuntimeException {
    public CustomMalformedJwtException(String message) {
        super(message);
    }
}