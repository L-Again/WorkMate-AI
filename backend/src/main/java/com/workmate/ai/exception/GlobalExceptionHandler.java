package com.workmate.ai.exception;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.common.ErrorCode;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingRequestHeaderException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public CommonResult<Void> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return CommonResult.failed(errorCode.getCode(), errorCode.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return CommonResult.failed(ErrorCode.PARAM_ERROR.getCode(), ErrorCode.PARAM_ERROR.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public CommonResult<Void> handleMissingRequestHeaderException(MissingRequestHeaderException exception) {
        return CommonResult.failed(ErrorCode.MISSING_USER.getCode(), ErrorCode.MISSING_USER.getMessage());
    }
}