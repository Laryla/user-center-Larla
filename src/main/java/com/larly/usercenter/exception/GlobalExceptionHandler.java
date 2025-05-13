package com.larly.usercenter.exception;

import com.larly.usercenter.common.BaseResponse;
import com.larly.usercenter.common.ErrorCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
//全局异常处理
@RestControllerAdvice//spring  aop 自带的异常处理
public class GlobalExceptionHandler {
    //捕获业务异常
    @ExceptionHandler(BusinessException.class)//括号里捕获的异常
    public BaseResponse businessExceptionHandler(BusinessException e) {
        return new BaseResponse(e.getCode(), e.getMessage(),"", e.getDescription());
    }
//    捕获java自带的异常
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e) {
        return new BaseResponse(ErrorCode.SERVER_ERROR.getCode(), e.getMessage(),"","");
    }
}
