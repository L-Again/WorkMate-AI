package com.workmate.ai.common;

public enum ErrorCode {

    PARAM_ERROR(40001, "请求参数错误"),
    MISSING_USER(40002, "缺少用户身份"),
    USER_NOT_FOUND_OR_DISABLED(40003, "用户不存在或停用"),
    FORBIDDEN(40301, "无权限"),
    DATA_NOT_FOUND(40401, "数据不存在"),
    STATE_CONFLICT(40901, "状态冲突"),
    SYSTEM_ERROR(50001, "系统错误"),
    DATABASE_ERROR(50002, "数据库失败"),
    REDIS_ERROR(50003, "Redis 失败"),
    LLM_ERROR(50004, "大模型调用失败"),
    ASYNC_LOG_ERROR(50005, "异步日志失败"),
    LLM_CALL_FAILED(50004, "大模型服务暂时不可用，请稍后重试");


    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}