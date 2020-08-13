package com.liuzw.redisson.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Redisson 基础信息配置
 *
 * @author liuzw
 */

@Data
@ConfigurationProperties(prefix = "spring.redis")
public class RedissonConfig {

    /**
     * 第几个数据库
     */
    private int database = 0;

    /**
     * 密码
     */
    private String password;

    /**
     * Redis server host.
     */
    private String host = "localhost";

    /**
     * Redis server port.
     */
    private int port = 6379;

    /**
     * 池配置
     */
    private Pool pool;

    /**
     * 集群 信息配置
     */
    private Cluster cluster;

    /**
     * 哨兵配置
     */
    private Sentinel sentinel;


    /**
     * redis 池配置
     **/
    @Data
    public static class Pool {

        /**
         * 最大空闲连接数
         */
        private int maxIdle = 8;

        /**
         * 最小空闲连接数
         */
        private int minIdle = 0;

        /**
         * 最大连接数
         */
        private int maxActive = 8;

        /**
         * 最大等待时间
         */
        private int maxWait = -1;

    }

    /**
     * Redis 集群 配置.
     */
    @Data
    public static class Cluster {

        /**
         * redis 集群地址 host:port 逗号分隔
         */
        private String nodes;
    }

    /**
     * Redis 哨兵 配置.
     */
    @Data
    public static class Sentinel {

        /**
         * redis master名称
         */
        private String master;

        /**
         * redis 节点 host:port
         */
        private String nodes;

    }

}
