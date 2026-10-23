package com.inno.controller.exception;

import com.inno.service.exception.AuthException;
import com.inno.service.exception.NotFoundException;
import com.inno.service.exception.UserServiceException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.inno.service.exception.ExceptionErrorCodes.AUTHENTICATION_FAILED;
import static com.inno.service.exception.ExceptionErrorCodes.CREDENTIAL_NOT_FOUND;
import static com.inno.service.exception.ExceptionErrorCodes.METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION;
import static com.inno.service.exception.ExceptionErrorCodes.VALIDATION_EXCEPTION;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiException apiException = new ApiException(
                VALIDATION_EXCEPTION.getErrorCode(),
                e.getLocalizedMessage(),
                status,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        return new ResponseEntity<>(apiException, status);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentTypeMismatchException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiException apiException = new ApiException(
                METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION.getErrorCode(),
                String.format(METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION.getErrorMessage(), e.getMessage()),
                status,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        return new ResponseEntity<>(apiException, status);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException e) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiException apiException = new ApiException(
                CREDENTIAL_NOT_FOUND.getErrorCode(),
                e.getMessage(),
                status,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        return new ResponseEntity<>(apiException, status);
    }


    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Object> handleValidationException(AuthException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ApiException apiException = new ApiException(
                AUTHENTICATION_FAILED.getErrorCode(),
                e.getLocalizedMessage(),
                status,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        return new ResponseEntity<>(apiException, status);
    }

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<Object> handleUserServiceException(UserServiceException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ApiException apiException = new ApiException(
                AUTHENTICATION_FAILED.getErrorCode(),
                e.getLocalizedMessage(),
                status,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        return new ResponseEntity<>(apiException, status);
    }
}
