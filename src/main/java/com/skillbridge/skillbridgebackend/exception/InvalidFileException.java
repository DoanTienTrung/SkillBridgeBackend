package com.skillbridge.skillbridgebackend.exception;

/**
 * Exception cho các lỗi validation file upload
 */
public class InvalidFileException extends RuntimeException {
    
    public InvalidFileException(String message) {
        super(message);
    }
    
    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}