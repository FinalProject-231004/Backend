package com.starta.project.global.exception.Custom;

/**
 * [JWT의 유효 기간이 만료되었을 때 발생하는 예외]
 * JWT에 설정한 유효 기간(Expiration Time)이 만료했으나 사용하려고 할 때 발생합니다.
  */

public class CustomExpiredJwtException extends RuntimeException {
    public CustomExpiredJwtException(String message) {
        super(message);
    }
}

