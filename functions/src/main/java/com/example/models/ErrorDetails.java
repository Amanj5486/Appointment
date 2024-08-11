package com.example.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ErrorDetails {
    private int code;
    private String status;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Object> details;

    public ErrorDetails(int code, String status, String message) {
        this(code, status, message, null);
    }
}