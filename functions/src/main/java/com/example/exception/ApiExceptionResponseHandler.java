package com.example.exception;


import com.example.models.BaseResponse;
import com.example.models.ErrorDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.ArrayList;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
@Slf4j
public class ApiExceptionResponseHandler {

    public ApiExceptionResponseHandler() {}

    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class
    })
    public ResponseEntity<BaseResponse<ErrorDetails>> handleMethodNotAllowed(Exception ex) {
        return getApiExceptionResponse(ex, METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler({
            MissingRequestHeaderException.class,
            MissingPathVariableException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<BaseResponse<ErrorDetails>> handleMissingOrBadRequestParams(Exception ex) {
        return getApiExceptionResponse(ex, BAD_REQUEST);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<BaseResponse<ErrorDetails>> handleInvalidArguments(MethodArgumentNotValidException ex) {
        HttpStatus status = BAD_REQUEST;
        ErrorDetails errDtls = new ErrorDetails(status.value(), status.getReasonPhrase(), "Invalid arguments", new ArrayList<>());
        if(ex!=null)
        ex.getAllErrors().forEach(e -> {
            log.error("Event Platform Service - Invalid Argument: {} | {}", status.value(), e.getDefaultMessage());
            errDtls.getDetails().add(e.getDefaultMessage());
        });

        BaseResponse<ErrorDetails> response = new BaseResponse<>(errDtls);
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler({
            ApplicationException.class
    })
    public ResponseEntity<BaseResponse<ErrorDetails>> handleApplicationException(ApplicationException e) {
        log.error("Event Platform Service - Application Exception: {} | {} | {}", e.getCode(), e.getStatus(), e.getMessage());
        ErrorDetails errDtls = new ErrorDetails(e.getCode(), e.getStatus(), e.getMessage(),e.getErrorDetails().getDetails());
        BaseResponse<ErrorDetails> response = new BaseResponse<>(errDtls);
        return new ResponseEntity<>(response, null, e.getCode());
    }

    @ExceptionHandler({
            RuntimeException.class
    })
    public ResponseEntity<BaseResponse<ErrorDetails>> handleRuntimeException(RuntimeException e) {
        ErrorDetails errDtls = new ErrorDetails(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR.getReasonPhrase(), e.getMessage());
        BaseResponse<ErrorDetails> resp = new BaseResponse<>(errDtls);
        return new ResponseEntity<>(resp, null, 500);
    }

    @ExceptionHandler({
            HttpStatusCodeException.class
    })
    public ResponseEntity<BaseResponse<ErrorDetails>> handleHttpStatusCodeException(HttpStatusCodeException e) {
        ErrorDetails errDtls = new ErrorDetails(e.getRawStatusCode(), e.getStatusText(), e.getResponseBodyAsString());
        BaseResponse<ErrorDetails> resp = new BaseResponse<>(errDtls);
        return new ResponseEntity<>(resp, e.getStatusCode());
    }

    private ResponseEntity<BaseResponse<ErrorDetails>> getApiExceptionResponse(Exception ex, HttpStatus status) {
        ErrorDetails errDtls = new ErrorDetails(status.value(), status.getReasonPhrase(), ex.getMessage());
        BaseResponse<ErrorDetails> resp = new BaseResponse<>(errDtls);
        return new ResponseEntity<>(resp, status);
    }
}
