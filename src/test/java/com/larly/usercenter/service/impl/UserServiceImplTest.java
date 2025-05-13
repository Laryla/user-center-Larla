package com.larly.usercenter.service.impl;

import com.larly.usercenter.mapper.UserMapper;
import com.larly.usercenter.model.domain.User;
import com.larly.usercenter.model.response.UserResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class UserServiceImplTest {
    @Resource
    private UserServiceImpl userService;
    @Autowired
    private UserMapper userMapper;

    @Test
    public void Test(){
        User user = new User();
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        map.put("key1","value");
        map.put("key2","value");
        map.put("key3","value1");
//        遍历
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
            System.out.println(entry.getValue());
//            if(entry.getValue().length() > 1){
//                System.out.println(entry.getKey());
//            }
        }

//
//        user.setUserAccount("theonefx");
//        Long i = userService.userRegister(user.getUserAccount(), "123", "123");

//        Assertions.assertEquals(-1, i);

//        System.out.println(i);

        user.setUserAccount("1231231231");
        user.setUserPassword("123213149");

//        String pattern = "^[a-zA-Z0-9_]*$";
//        System.out.println(user.getUserAccount().matches(pattern));
//        final String SALT = "larly";
//        String newPassword = DigestUtils.md5DigestAsHex(( SALT + user.getUserPassword()).getBytes());
//        System.out.println(newPassword);
//        System.out.println(userService.userRegister(user.getUserAccount(), user.getUserPassword(), user.getUserPassword()));
    }


    @Test
    void searchUsersByTags() {
    }
}