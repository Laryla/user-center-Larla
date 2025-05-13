package com.larly.usercenter.mapper;

import com.larly.usercenter.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class UserMapperTest {

    @Resource
    private UserMapper userMapper;

    @Test
    void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        User user = new User();
        user.setUserName("theonefx");
        user.setEmail("theonefx@foxmail.com");
        user.setUserAccount("theonefx");
        user.setUserPassword("12345678");
        userMapper.insert(user);
        System.out.println(userMapper.selectById(user.getId()));
    }

}