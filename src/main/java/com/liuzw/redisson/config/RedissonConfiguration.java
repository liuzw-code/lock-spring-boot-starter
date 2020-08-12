package com.liuzw.redisson.config;

import com.liuzw.redisson.DistributedLockImpl;
import com.liuzw.redisson.IDistributedLock;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson 配置
 *
 * @author liuzw
 */

@Configuration
@AutoConfigureAfter(RedissonConfig.class)
@EnableConfigurationProperties({RedissonConfig.class, RedissonSentinelConfig.class})
public class RedissonConfiguration {

    @Autowired
    private RedissonConfig redissonConfig;

    @Autowired
    private RedissonSentinelConfig redissonSentinelConfig;

    /**
     * 哨兵模式自动装配
     *
     * @return RedissonClient
     */
   /* @Bean
    public RedissonClient redissonSentinel() {
        Config config = new Config();
        String address = redissonSentinelConfig.getSentinelAddresses();
        SentinelServersConfig serverConfig = config.useSentinelServers()
                .addSentinelAddress(address.split(","))
                .setMasterName(redissonSentinelConfig.getMasterName());
        if(StringUtils.isNotBlank(redissonSentinelConfig.getPassword())) {
            serverConfig.setPassword(redissonSentinelConfig.getPassword());
        }
        return Redisson.create(config);
    }*/


    /**
     * 单机模式自动装配
     *
     * @return redissonClient
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress("redis://" + redissonConfig.getHost() + ":" + redissonConfig.getPort());
        if (StringUtils.isNotBlank(redissonConfig.getPassword())) {
            serverConfig.setPassword(redissonConfig.getPassword());
        }
        return Redisson.create(config);
    }

    @Bean
    public IDistributedLock distributedLock() {
        return new DistributedLockImpl();
    }

}
