package com.ntd.uniorien.utils.helper;


import com.ntd.uniorien.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;

public class ResponseUtils {

    public static <T> ApiResponse<T> buildResponse(T result, String message, HttpStatus status) {
        return ApiResponse.<T>builder()
                .code(status.value())
                .message(message)
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> ok(T result) {
        return buildResponse(result, "Success", HttpStatus.OK);
    }

    public static <T> ApiResponse<T> created(T result) {
        return buildResponse(result, "Created", HttpStatus.CREATED);
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        return ApiResponse.<T>builder()
                .code(status.value())
                .message(message)
                .build();
    }
}
