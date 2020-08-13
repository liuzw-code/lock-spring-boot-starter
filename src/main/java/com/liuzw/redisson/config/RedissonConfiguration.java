package com.liuzw.redisson.config;

import com.liuzw.redisson.DistributedLockImpl;
import com.liuzw.redisson.IDistributedLock;
import com.liuzw.redisson.aop.DistributedLockAspect;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * redisson 配置
 *
 * @author liuzw
 */

@Configuration
@AutoConfigureAfter(RedissonConfig.class)
@EnableConfigurationProperties({RedissonConfig.class})
public class RedissonConfiguration {

    @Autowired
    private RedissonConfig redissonConfig;


    /**
     * 自动装配
     *
     * @return redissonClient
     */
    @Bean
    public RedissonClient redissonClient() {
        RedissonConfig.Cluster cluster = redissonConfig.getCluster();
        if (cluster != null) {
            return redissonCluster();
        }
        RedissonConfig.Sentinel sentinel = redissonConfig.getSentinel();
        if (sentinel != null) {
            return redissonSentinel();
        }
        return redissonSingle();
    }

    /**
     * 注入分布式锁
     */
    @Bean
    public IDistributedLock distributedLock() {
        return new DistributedLockImpl();
    }

    @Bean
    public DistributedLockAspect distributedLockAspect() {
        return new DistributedLockAspect();
    }

    /**
     * 单机模式自动装配
     *
     * @return redissonClient
     */
    private RedissonClient redissonSingle() {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress("redis://" + redissonConfig.getHost() + ":" + redissonConfig.getPort());
        if (StringUtils.isNotBlank(redissonConfig.getPassword())) {
            serverConfig.setPassword(redissonConfig.getPassword());
        }

        return Redisson.create(config);
    }


    /**
     * 集群模式的 配置
     *
     * @return RedissonClient
     */
    private RedissonClient redissonCluster() {
        Config config = new Config();
        String nodes = redissonConfig.getCluster().getNodes();
        //校验
        checkRedisUrl(nodes);
        ClusterServersConfig serverConfig = config.useClusterServers()
                .addNodeAddress(nodes);
        if (StringUtils.isNotBlank(redissonConfig.getPassword())) {
            serverConfig.setPassword(redissonConfig.getPassword());
        }
        return Redisson.create(config);
    }


    /**
     * 哨兵模式自动装配
     *
     * @return RedissonClient
     */
    private RedissonClient redissonSentinel() {
        Config config = new Config();
        RedissonConfig.Sentinel sentinel = redissonConfig.getSentinel();
        String nodes = sentinel.getNodes();
        //校验
        checkRedisUrl(nodes);
        SentinelServersConfig serverConfig = config.useSentinelServers()
                .addSentinelAddress(nodes.split(","))
                .setMasterName(sentinel.getMaster());
        if (StringUtils.isNotBlank(redissonConfig.getPassword())) {
            serverConfig.setPassword(redissonConfig.getPassword());
        }
        return Redisson.create(config);
    }


    /**
     * 校验redis 地址是否填写正确
     */
    private void checkRedisUrl(String nodes) {
        if (StringUtils.isBlank(nodes)) {
            throw new IllegalStateException(
                    "Invalid redis nodes is not empty");
        }
        for (String node : org.springframework.util.StringUtils
                .commaDelimitedListToStringArray(nodes)) {
            try {
                String[] parts = org.springframework.util.StringUtils.split(node, ":");
                Assert.state(parts.length == 2, "Must be defined as 'host:port'");
            } catch (RuntimeException ex) {
                throw new IllegalStateException(
                        "Invalid redis property '" + node + "'", ex);
            }
        }
    }

}
