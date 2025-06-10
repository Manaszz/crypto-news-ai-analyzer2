package com.cryptonews.mcpserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class PerplexityApiException extends RuntimeException {
    public PerplexityApiException(String message) {
        super(message);
    }

    public PerplexityApiException(String message, Throwable cause) {
        super(message, cause);
    }
} 