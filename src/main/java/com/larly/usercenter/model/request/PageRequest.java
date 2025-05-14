package com.larly.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = -6910549061974283929L;
    /**
     * 当前页
     */
    private Integer pageNum ;
    /**
     * 页大小
     */
    private Integer pageSize;
}
