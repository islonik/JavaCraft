package dev.nklip.javacraft.ses.app.controller;

import dev.nklip.javacraft.ses.api.shared.ErrorResponse;
import dev.nklip.javacraft.ses.app.exception.InvalidWorkRequestTransitionException;
import dev.nklip.javacraft.ses.app.exception.UnknownBudgetCodeException;
import dev.nklip.javacraft.ses.app.exception.WorkRequestNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@SuppressWarnings("unused")
public class RestExceptionHandler {

    @ExceptionHandler(WorkRequestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            WorkRequestNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidWorkRequestTransitionException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            InvalidWorkRequestTransitionException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(UnknownBudgetCodeException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            UnknownBudgetCodeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        ));
    }
}
