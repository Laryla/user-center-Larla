package com.larly.usercenter.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserSearchParam {
    private String username;

//    当前页数
    private Integer pageNum = 1;
//    每页个数
    private Integer pageSize = 5;
}
