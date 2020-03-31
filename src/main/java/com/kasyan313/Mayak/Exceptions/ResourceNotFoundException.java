package com.kasyan313.Mayak.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ResourceNotFoundException extends RuntimeException{
    @Override
    public String getMessage() {
        return "resource not found";
    }
}
