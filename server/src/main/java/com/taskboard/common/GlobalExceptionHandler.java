package com.taskboard.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Global exception handler that converts exceptions to ApiResponse format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Error item structure for validation/business errors.
     */
    public record ErrorItem(int code, String field, String message) {
        public ErrorItem(int code, String message) {
            this(code, null, message);
        }
    }

    /**
     * Extended ApiResponse with errors array.
     */
    public record ApiErrorResult(int code, String message, Object data, List<ErrorItem> errors) {
    }

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiErrorResult> handleBizException(BizException ex) {
        int httpStatus = resolveHttpStatus(ex.getErrorCode());
        ApiErrorResult result = new ApiErrorResult(
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                List.of(new ErrorItem(ex.getErrorCode(), ex.getMessage()))
        );
        return ResponseEntity.status(httpStatus).body(result);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResult> handleResponseStatusException(ResponseStatusException ex) {
        int httpStatus = ex.getStatusCode().value();
        int errorCode = mapToErrorCode(httpStatus);
        ApiErrorResult result = new ApiErrorResult(
                errorCode,
                ex.getReason() != null ? ex.getReason() : ex.getMessage(),
                null,
                List.of(new ErrorItem(errorCode, ex.getReason() != null ? ex.getReason() : ex.getMessage()))
        );
        return ResponseEntity.status(httpStatus).body(result);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResult> handleIllegalArgument(IllegalArgumentException ex) {
        ApiErrorResult result = new ApiErrorResult(
                40005,
                ex.getMessage(),
                null,
                List.of(new ErrorItem(40005, ex.getMessage()))
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    private int resolveHttpStatus(int errorCode) {
        if (errorCode >= 42000 && errorCode < 43000) {
            return HttpStatus.BAD_REQUEST.value();
        }
        if (errorCode == 40003) {
            return HttpStatus.CONFLICT.value();
        }
        if (errorCode == 40004) {
            return HttpStatus.NOT_FOUND.value();
        }
        if (errorCode >= 49000) {
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        return HttpStatus.BAD_REQUEST.value();
    }

    private int mapToErrorCode(int httpStatus) {
        return switch (httpStatus) {
            case 404 -> 40004;
            case 409 -> 40003;
            default -> 40005;
        };
    }
}
