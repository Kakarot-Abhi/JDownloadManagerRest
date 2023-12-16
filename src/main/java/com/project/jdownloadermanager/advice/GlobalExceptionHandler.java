package com.project.jdownloadermanager.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        // Log the exception (you might want to log it using a logging framework like SLF4J)
        ex.printStackTrace();

        // Create a custom error response
//        ErrorResponse errorResponse = new ErrorResponse("An unexpected error occurred.");

        return new ResponseEntity<>(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // You can add more @ExceptionHandler methods for specific exception types if needed
}
