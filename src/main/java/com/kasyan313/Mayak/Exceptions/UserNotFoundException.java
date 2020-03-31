package com.kasyan313.Mayak.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class UserNotFoundException extends RuntimeException {
    @Override
    public String getMessage() {
        return "user not found";
    }
}

