package com.example.doublecross.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleGameNotFound(GameNotFoundException ex) {
        log.warn("Game not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(InvalidTurnException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTurn(InvalidTurnException ex) {
        log.warn("Invalid turn: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("INVALID_TURN", ex.getMessage()));
    }

    @ExceptionHandler(GameException.class)
    public ResponseEntity<Map<String, Object>> handleGameException(GameException ex) {
        log.warn("Game error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("GAME_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation error: {}", fieldErrors);

        Map<String, Object> response = createErrorResponse("VALIDATION_ERROR", "입력값이 올바르지 않습니다.");
        response.put("fieldErrors", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("INTERNAL_ERROR", "서버 오류가 발생했습니다."));
    }

    private Map<String, Object> createErrorResponse(String code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        response.put("timestamp", OffsetDateTime.now().toString());
        return response;
    }
}
