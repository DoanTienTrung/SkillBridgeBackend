package com.skillbridge.skillbridge_backend.exception;

import com.skillbridge.skillbridge_backend.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                "Dữ liệu không hợp lệ",
                "Validation failed",
                request.getDescription(false)
        );
        response.setData(errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle user not found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                "User not found",
                request.getDescription(false)
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Handle email already exists
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex, WebRequest request) {

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                "Email already exists",
                request.getDescription(false)
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Handle bad credentials (wrong password)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {

        ApiResponse<Object> response = ApiResponse.error(
                "Email hoặc mật khẩu không đúng",
                "Invalid credentials",
                request.getDescription(false)
        );

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // Handle file storage exceptions
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileStorageException(
            FileStorageException ex, WebRequest request) {

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                "File storage error",
                request.getDescription(false)
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle invalid file exceptions
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidFileException(
            InvalidFileException ex, WebRequest request) {

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                "Invalid file",
                request.getDescription(false)
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle file size exceeded exception
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxSizeException(
            MaxUploadSizeExceededException ex, WebRequest request) {

        ApiResponse<Object> response = ApiResponse.error(
                "File quá lớn. Kích thước tối đa cho phép là 50MB",
                "File size exceeded",
                request.getDescription(false)
        );

        return new ResponseEntity<>(response, HttpStatus.PAYLOAD_TOO_LARGE);
    }
    @ExceptionHandler(LessonNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleLessonNotFoundException(
            LessonNotFoundException ex, WebRequest request) {

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                "Lesson not found",
                request.getDescription(false)
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Handle generic runtime exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        ApiResponse<Object> response = ApiResponse.error(
                "Có lỗi xảy ra trong hệ thống",
                ex.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        ApiResponse<Object> response = ApiResponse.error(
                "Lỗi không xác định",
                ex.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}