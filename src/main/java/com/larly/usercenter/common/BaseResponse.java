package com.larly.usercenter.common;

import lombok.Data;

/**
 * 通用返回类
 * @param <T>
 */
@Data
public class BaseResponse<T> {
    private Integer code;
    private String msg;
    private T data;
    private String description;

    public BaseResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public BaseResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public BaseResponse(Integer code, String msg, T data, String description) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.description = description;
    }
    public BaseResponse(ErrorCode errorCode,String msg, String description){
        this.code = errorCode.getCode();
        this.msg = errorCode.getMessage();
        this.description = description;
        this.data = null;
    }
    public BaseResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.msg = errorCode.getMessage();
        this.description = errorCode.getDescription();
        this.data = null;
    }
}
