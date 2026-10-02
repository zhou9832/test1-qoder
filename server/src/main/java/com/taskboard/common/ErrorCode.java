package com.taskboard.common;

/**
 * Error code constants following the segmented allocation scheme:
 * 40xxx - parameter/validation errors
 * 42xxx - state transition errors
 * 49xxx - internal errors
 */
public final class ErrorCode {

    private final int code;
    private final String message;

    private ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    // 40xxx: Parameter & validation errors
    public static final ErrorCode PARAM_REQUIRED = new ErrorCode(40001, "必填项缺失");
    public static final ErrorCode PARAM_LENGTH_EXCEEDED = new ErrorCode(40002, "超出长度限制");
    public static final ErrorCode PARAM_DUPLICATE = new ErrorCode(40003, "名称已存在");
    public static final ErrorCode PARAM_NOT_FOUND = new ErrorCode(40004, "资源不存在");
    public static final ErrorCode PARAM_INVALID = new ErrorCode(40005, "参数值无效");

    // 42xxx: State transition errors
    public static final ErrorCode TRANSITION_ILLEGAL = new ErrorCode(42001, "不允许从当前状态直接转移到目标状态");
    public static final ErrorCode TRANSITION_TERMINAL = new ErrorCode(42002, "任务已关闭，不可再修改");

    // 49xxx: Internal errors
    public static final ErrorCode INTERNAL_DB = new ErrorCode(49001, "数据库异常");
}
