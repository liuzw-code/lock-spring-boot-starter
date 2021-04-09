package com.liuzw.redisson;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式锁 配置
 *
 * @author liuzw
 */
@Configuration
@RequiredArgsConstructor
@AutoConfigureAfter(RedissonAutoConfiguration.class)
@EnableConfigurationProperties({RedissonConfigProperties.class})
public class DistributedLockConfiguration {


    private final RedissonClient redissonClient;


    @Bean
    public IDistributedLock distributedLock(RedissonConfigProperties properties) {
        return new DefaultDistributedLock(redissonClient, properties);
    }

    @Bean
    public DistributedLockAspect distributedLockAspect(IDistributedLock distributedLock) {
        return new DistributedLockAspect(distributedLock);
    }

}
