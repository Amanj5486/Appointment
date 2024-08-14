package com.example.appointment.exception;

import com.example.appointment.models.ErrorDetails;
import com.example.appointment.models.ApplicationError;
import lombok.Getter;

import java.io.Serial;

@Getter
public class ApplicationException extends RuntimeException {
	
	@Serial
    private static final long serialVersionUID = 1L;

	private final int code;
    private final String status;
    private final ErrorDetails errorDetails;

    public ApplicationException(int code, String status, String message) {
        this(code, status, message, null);
    }

    public ApplicationException(int code, String status, String message, Throwable ex) {
        super(message, ex);

        this.code = code;
        this.status = status;
        this.errorDetails = new ErrorDetails(code, status, message);
    }

    public ApplicationException(ApplicationError err) {
        this(err.getErrorDetails());
    }

    public ApplicationException(ErrorDetails errorDetails) {
        super(errorDetails.getMessage());

        this.code = errorDetails.getCode();
        this.status = errorDetails.getStatus();

        this.errorDetails = errorDetails;
    }
}
