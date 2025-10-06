package com.ntd.uniorien.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_ERROR(666, "Uncategorized Error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOTFOUND(1001, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTED(1002, "User already exists", HttpStatus.CONFLICT),
    UNAUTHENTICATED(1003, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "You dont have permission", HttpStatus.FORBIDDEN),
    PASSWORD_EXISTED(1005, "Password existed", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(1006, "Your account is locked", HttpStatus.BAD_REQUEST),
    ;

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}
