package com.larly.usercenter.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
//    总数
    private Long total;
//    当前页数
    private Integer pageNum;
//    数据
    private List<T> list;

}
