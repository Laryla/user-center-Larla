package com.larly.usercenter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void testRedis() {
        redisTemplate.opsForValue().set("name", "larly");
//        断言
        Assertions.assertTrue("larly".equals(redisTemplate.opsForValue().get("name")));
        System.out.println(redisTemplate.opsForValue().get("name"));
    }
}
