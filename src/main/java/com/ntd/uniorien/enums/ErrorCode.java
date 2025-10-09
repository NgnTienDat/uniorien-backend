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
    // App: 1XXX
    UNCATEGORIZED_ERROR(1000, "Uncategorized Error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOTFOUND(1001, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTED(1002, "User already exists", HttpStatus.CONFLICT),
    UNAUTHENTICATED(1003, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "You dont have permission", HttpStatus.FORBIDDEN),
    PASSWORD_EXISTED(1005, "Password existed", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(1006, "Your account is locked", HttpStatus.BAD_REQUEST),

    // Validation: 2XXX
    INVALID_MESSAGE_KEY(2001, "Invalid Message Key", HttpStatus.BAD_REQUEST),
    NOT_BLANK(2002, "Cannot blank this field", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(2003, "Invalid email address" , HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(2004, "Invalid password", HttpStatus.BAD_REQUEST),
    INVALID_NAME(2005, "Invalid name account", HttpStatus.BAD_REQUEST),

    // Connect: 3XXX
    CRAWL_URL_CONNECTION_ERROR(3001, "Crawl URL Connection Error", HttpStatus.INTERNAL_SERVER_ERROR),


    ;

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}
