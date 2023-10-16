package com.starta.project.global.exception.Custom;

/**
 * [JWT의 유효 기간이 만료되었을 때 발생하는 예외]
 * JWT에는 유효 기간(Expiration Time)이 있을 수 있습니다.
 * 이 시간이 경과하면 토큰은 더 이상 유효하지 않게 됩니다.
 * 해당 예외는 이러한 토큰을 사용하려고 할 때 발생합니다.
  */


public class CustomExpiredJwtException extends RuntimeException {
    public CustomExpiredJwtException(String message) {
        super(message);
    }
}

