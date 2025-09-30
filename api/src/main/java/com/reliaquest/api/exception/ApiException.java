package com.reliaquest.api.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
