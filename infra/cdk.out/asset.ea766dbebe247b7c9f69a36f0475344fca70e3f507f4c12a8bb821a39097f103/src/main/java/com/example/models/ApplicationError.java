package com.example.models;

import lombok.Getter;

@Getter
public enum ApplicationError {
    USER_EXIST(404, "username / user  already exist ", "username already exits"),
    APPOINTMENT_NOT_FOUND(404, "appointment not found ", "appointment not found");


    private final int code;
    private final String status;
    private final String message;
    private final ErrorDetails errorDetails;

    ApplicationError(int code, String status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;

        this.errorDetails = new ErrorDetails(code, status, message);
    }
}
