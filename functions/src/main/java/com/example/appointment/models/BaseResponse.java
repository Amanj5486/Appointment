package com.example.appointment.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class BaseResponse<T> {
    private String type;
    private String timestamp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ErrorDetails error;

    public BaseResponse() {
        this.timestamp = Instant.now().toString();
        this.data = null;
        this.error = null;
    }

    public BaseResponse(T data, String type) {
        this();
        this.data = data;
        this.type = type;
    }

    public BaseResponse(ErrorDetails error) {
        this();
        this.error = error;
    }
}