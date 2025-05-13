package com.larly.usercenter.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//拿到yaml配置文件
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissionConfig {

    private String host;

    private String port;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
//        这里用的单机  Redis
        config.useSingleServer().setAddress(redisAddress);
                // use "redis://" for Redis connection
                // use "valkey://" for Valkey connection
                // use "valkeys://" for Valkey SSL connection
                // use "rediss://" for Redis SSL connection;

        // Sync and Async API
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
