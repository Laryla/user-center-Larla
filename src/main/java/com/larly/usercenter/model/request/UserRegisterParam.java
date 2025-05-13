package com.larly.usercenter.model.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterParam {
    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String phone;
    private Integer gender;
    private String plantCode;
}