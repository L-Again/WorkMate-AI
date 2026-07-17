package com.workmate.ai.exception;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.common.ErrorCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public CommonResult<Void> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return CommonResult.failed(errorCode.getCode(), errorCode.getMessage());
    }
}