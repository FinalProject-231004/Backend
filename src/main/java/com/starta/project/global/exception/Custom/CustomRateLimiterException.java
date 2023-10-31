package com.starta.project.global.exception.Custom;

import com.google.common.util.concurrent.RateLimiter;

public class CustomRateLimiterException  extends RuntimeException{

    public CustomRateLimiterException (String message) {
        super(message);
    }
}
