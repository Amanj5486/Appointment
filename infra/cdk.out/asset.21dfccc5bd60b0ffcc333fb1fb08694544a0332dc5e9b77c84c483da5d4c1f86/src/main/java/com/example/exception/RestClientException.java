package com.example.exception;

public class RestClientException extends RuntimeException {

    private final String errorMessage;


    public RestClientException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }


    public String getErrorMessage() {
        return errorMessage;
    }


}
