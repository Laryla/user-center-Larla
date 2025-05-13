package com.larly.usercenter;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class RedissionTest {

    @Resource
    private RedissonClient redissonClient;
    @Test
    public void testRedission() {
        // 存储数据
        RBucket<String> bucket = redissonClient.getBucket("testKey");
        bucket.set("Hello Redisson!");

        // 获取数据
        String value = bucket.get();

        // 断言验证
        assertThat(value).isEqualTo("Hello Redisson!");

//        数组
        RList<Object> list = redissonClient.getList("testKey1");
        list.add("Hello Redisson1!");
    }

//    测试一下分布式锁
    @Test
    public void testRedissionLock(){
//        创建分布式锁
        RLock lock = redissonClient.getLock("lock");
//        配置分布式锁
//         参数：等待时间，过期时间，时间单位,
//         如果第二个参数是-1，，redission会使用看门狗机制默认30秒，如果在10秒内没有释放锁，redission会自动续期
        try {
            if(lock.tryLock(0,10, TimeUnit.SECONDS)){
//            拿到锁
                System.out.println("拿到锁了");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
//                释放自己的锁
                lock.unlock();
            }
        }

    }

}
