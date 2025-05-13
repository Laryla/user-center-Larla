package com.larly.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.larly.usercenter.mapper.UserMapper;
import com.larly.usercenter.model.domain.User;
import com.larly.usercenter.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 预缓存任务(定时任务)
 */

@Component
public class PreCacheJob {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;



//    重点用户
    private static final List<Long> mainUserList = Arrays.asList(1L, 2L, 3L);

    @Scheduled(cron = "0 0 12 * * ?")
    public void preCache() {
//        这里可以改造成分布式锁（使用redission）

        for (Long userId : mainUserList){
            // 预缓存
            ValueOperations valueOperations = redisTemplate.opsForValue();
            // 构建 Redis 缓存键
            String redisKey = String.format("user:recommend:%s", userId);
            // 将结果写入缓存（设置5分钟过期时间）
            Page<User> page = PageHelper.startPage( 1, 20 );
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            List<User> userList = userMapper.selectList(queryWrapper);
            valueOperations.set(redisKey, page, 5, TimeUnit.MINUTES);
        }

    }
}
