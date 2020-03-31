package com.kasyan313.Mayak.Controllers;

import com.kasyan313.Mayak.Exceptions.ResourceNotFoundException;
import com.kasyan313.Mayak.Exceptions.UserAlreadyExistsException;
import com.kasyan313.Mayak.Exceptions.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Controller
public class ErrorController {
    @ExceptionHandler({ResourceNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<String> catchNotFoundException(RuntimeException exc) {
        return ResponseEntity.status(404).body(exc.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> catchAlreadyExistsException(RuntimeException exc) {
        return ResponseEntity.status(406).body(exc.getMessage());
    }
}
