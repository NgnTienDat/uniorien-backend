package com.ntd.uniorien.exception;

import com.ntd.uniorien.dto.response.ApiResponse;
import com.ntd.uniorien.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<?>> handlingRuntimeException(Exception exception) {
        log.info("Exception: ", exception);

        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(false);
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_ERROR.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_ERROR.getMessage());

        return ResponseEntity
                .status(ErrorCode.UNCATEGORIZED_ERROR.getHttpStatusCode())
                .body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        log.info("App exception: ", exception);

        ErrorCode errorCode = exception.getErrorCode();

        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(false);
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        log.info("Access denied exception: ", exception);

        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(false);
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        log.info("MethodArgumentNotValidException: ", exception);

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            String key = error.getDefaultMessage();

            ErrorCode errorCode;
            try {
                errorCode = ErrorCode.valueOf(key.trim());
            } catch (IllegalArgumentException | NullPointerException e) {
                errorCode = ErrorCode.INVALID_MESSAGE_KEY;
            }

            errors.put(error.getField(), errorCode.getMessage());
        }

        ApiResponse<Map<String, String>> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage("Validation Failed");
        response.setResult(errors);


        return ResponseEntity.badRequest().body(response);
    }
}
