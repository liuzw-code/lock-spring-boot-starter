package com.liuzw.redisson.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 哨兵配置信息
 *
 * @author liuzw
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.redisson")
public class RedissonSentinelConfig {

    /**
     * 集群地址， 逗号分隔
     */
    private String sentinelAddresses;

    /**
     * 主 服务器名称
     */
    private String masterName;

    /**
     * 密码
     */
    private String password;


}