package com.starta.project.global.exception.Custom;
/*
api 요청에 초당 횟수 제한을걸어 너무 많은 요청을 방지하기 위한 예외처리
 */

public class CustomRateLimiterException  extends RuntimeException{

    public CustomRateLimiterException (String message) {
        super(message);
    }
}
