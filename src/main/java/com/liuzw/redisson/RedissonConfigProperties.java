package com.liuzw.redisson;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author liuzw
 */
@Data
@ConfigurationProperties(prefix = RedissonConfigProperties.REDISSON_PREFIX)
public class RedissonConfigProperties {

    public static final String REDISSON_PREFIX = "spring.liuzw.redisson";

    /**
     * 默认等待时间
     */
    private Integer waitTime = 0;

    /**
     * 默认超时时间
     */
    private Integer leaseTime = 30000;

    /**
     * 时间粒度 单位默认为hao秒
     */
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
}
