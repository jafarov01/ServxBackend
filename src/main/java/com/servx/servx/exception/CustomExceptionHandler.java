package com.servx.servx.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
public class CustomExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    // validation errors
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        logError(ex, request.getRequestURI(), HttpStatus.BAD_REQUEST);
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // illegal argument or illegal state errors
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        logError(ex, request.getRequestURI(), HttpStatus.BAD_REQUEST);
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // resource not found error
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        logError(ex, request.getRequestURI(), HttpStatus.NOT_FOUND);
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request.getRequestURI());
    }

    // generic exceptions
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex, HttpServletRequest request) {
        logError(ex, request.getRequestURI(), HttpStatus.INTERNAL_SERVER_ERROR);
        return buildErrorResponse("An unexpected error occurred. Please, try again later.", HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI());
    }

    private ErrorResponse buildErrorResponse(String message, HttpStatus status, String path) {
        return ErrorResponse.builder()
                .message(message)
                .statusCode(status.value())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .path(path)
                .build();
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, HttpServletRequest request) {
        logError(ex, request.getRequestURI(), HttpStatus.BAD_REQUEST);
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentialsException(InvalidCredentialsException ex, HttpServletRequest request) {
        logError(ex, request.getRequestURI(), HttpStatus.UNAUTHORIZED);
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, request.getRequestURI());
    }

    private void logError(Exception ex, String path, HttpStatus status) {
        logger.error("Error occurred at path: {}, Status: {}, Message: {}", path, status, ex.getMessage(), ex);
    }
}

