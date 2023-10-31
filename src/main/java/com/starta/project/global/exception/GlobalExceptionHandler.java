package com.starta.project.global.exception;

import com.starta.project.global.exception.Custom.*;
import com.starta.project.global.messageDto.MsgResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<RestApiException> illegalArgumentExceptionHandler(IllegalArgumentException ex) {
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(
                // HTTP body
                restApiException,
                // HTTP status code
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({NullPointerException.class})
    public ResponseEntity<RestApiException> nullPointerExceptionHandler(NullPointerException ex) {
        RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(
                // HTTP body
                restApiException,
                // HTTP status code
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

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<RestApiException> handleValidationExceptions(MethodArgumentNotValidException ex) {
//        StringBuilder errorMessage = new StringBuilder("Validation Failed: ");
//
//        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
//            errorMessage.append(fieldError.getField())
//                    .append(" - ")
//                    .append(fieldError.getDefaultMessage())
//                    .append("; ");
//        }
//
//        RestApiException restApiException = new RestApiException(errorMessage.toString(), HttpStatus.BAD_REQUEST.value());
//        return new ResponseEntity<>(restApiException, HttpStatus.BAD_REQUEST);
//    }
}