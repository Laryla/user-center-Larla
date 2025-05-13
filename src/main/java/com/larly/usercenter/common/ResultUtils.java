package com.larly.usercenter.common;

/**
 * 通用返回类结果的工具类
 */
public class ResultUtils {
    public static <T> BaseResponse<T> success( T data){
        return new BaseResponse<>(1, "ok",data);
    }
    public static BaseResponse error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode.getCode(),errorCode.getMessage(),null);
    }
}
