package com.larly.usercenter.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginParam {

    private String userAccount;
    private String userPassword;

}
