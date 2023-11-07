package com.starta.project.global.exception;


import com.starta.project.global.exception.custom.*;
import com.starta.project.global.messageDto.MsgResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<RestApiException> illegalArgumentExceptionHandler(IllegalArgumentException ex) {
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(
                restApiException,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({NullPointerException.class})
    public ResponseEntity<RestApiException> nullPointerExceptionHandler(NullPointerException ex) {
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(
                restApiException,
                HttpStatus.NOT_FOUND
        );
    }
    @ExceptionHandler({CustomExpiredJwtException.class})
    public ResponseEntity<RestApiException> customExpiredJwtExceptionHandler(CustomExpiredJwtException ex) {
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(
                restApiException,
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler({CustomInvalidJwtException.class})
    public ResponseEntity<RestApiException> customInvalidJwtExceptionHandler(CustomInvalidJwtException ex) {
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(
                restApiException,
                HttpStatus.FORBIDDEN
        );
    }
    @ExceptionHandler({CustomMalformedJwtException.class})
    public ResponseEntity<RestApiException> customMalformedJwtExceptionHandler(CustomMalformedJwtException ex) {
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(
                restApiException,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({CustomUnsupportedJwtException.class})
    public ResponseEntity<RestApiException> customUnsupportedJwtExceptionHandler(CustomUnsupportedJwtException ex) {
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(
                restApiException,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({CustomRateLimiterException.class})
    public ResponseEntity<RestApiException> customRateLimiterExceptionHandler (CustomRateLimiterException ex) {
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS.value());
        return new ResponseEntity<>(
                restApiException,
                HttpStatus.TOO_MANY_REQUESTS
        );
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public final void handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, WebRequest request) {
       log.info("Async request timed out Resolved [org.springframework.web.context.request.async.AsyncRequestTimeoutException]");
    }

    @ExceptionHandler(CustomKakaoBlockException.class)
    public ResponseEntity<MsgResponse> handleCustomException(CustomKakaoBlockException ex) {
        MsgResponse errorResponse = new MsgResponse(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
}