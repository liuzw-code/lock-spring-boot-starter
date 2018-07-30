package com.liuzw.redisson.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redisson 基础信息配置
 *
 * @author liuzw
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.redis")
public class RedissonConfig {

    /**
     * redis 服务器地址
     */
    private String host;
    /**
     * redis 密码
     */
    private String password;

    /**
     * 第几个数据库
     */
    private Integer database = 0;

    /**
     * 端口号
     */
    private Integer port;
}
