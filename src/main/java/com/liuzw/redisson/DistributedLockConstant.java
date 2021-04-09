package com.liuzw.redisson;

/**
 * 常量
 *
 * @author liuzw
 */

public class DistributedLockConstant {

    private DistributedLockConstant() {
    }


    /**
     * redis锁的前缀
     */
    public static final String PREFIX_LOCK_NAME = "redis:distributed:lock:";


    /**
     * redis锁的前缀
     */
    public static final String PARAM_SPEL = "#";

    /**
     * 应用名
     */
    public static final String APPLICATION_NAME = "spring.application.name";

}
