package com.starta.project.global.exception.Custom;

/**
 * [JWT의 서명이 올바르지 않거나 JWT의 구조가 예상과 다를 때 발생하는 예외]
 * JWT는 서명을 사용하여 데이터의 무결성을 보장하는데
 * 토큰이 중간에 변경되거나, 서명이 잘못된 경우 (예: 키 변경), 구조가 잘못된 경우에 이 예외가 발생합니다.
 */


public class CustomInvalidJwtException extends RuntimeException {
    public CustomInvalidJwtException(String message) {
        super(message);
    }
}
