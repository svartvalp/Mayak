package com.kasyan313.Mayak.Controllers;

import com.kasyan313.Mayak.Exceptions.ResourceNotFoundException;
import com.kasyan313.Mayak.Exceptions.UserAlreadyExistsException;
import com.kasyan313.Mayak.Exceptions.UserNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ErrorController {
    @ExceptionHandler({ResourceNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<Map<String, Object>> catchNotFoundException(RuntimeException exc) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", exc.getMessage());
        return ResponseEntity.status(404).body(body);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> catchAlreadyExistsException(RuntimeException exc) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", exc.getMessage());
        return ResponseEntity.status(406).body(body);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> catchValidationException(MethodArgumentNotValidException exc) {
        Map<String, Object> body = new HashMap<>();
        body.put("message",exc.getBindingResult().getFieldErrors()
                .stream().map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage()).collect(joining("; ")));
        return ResponseEntity.status(400).body(body);
    }
}
