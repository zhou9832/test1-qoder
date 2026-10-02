package com.taskboard.common;

/**
 * Business exception carrying error code and message.
 * Caught by GlobalExceptionHandler and converted to ApiResponse with errors array.
 */
public class BizException extends RuntimeException {

    private final int errorCode;

    public BizException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode.getCode();
    }

    public int getErrorCode() {
        return errorCode;
    }
}
