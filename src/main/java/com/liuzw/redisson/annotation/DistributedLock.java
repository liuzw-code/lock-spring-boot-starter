package com.liuzw.redisson.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author liuzw
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 如果lockName可以确定，直接设置该属性
     *
     */
    String lockName() default "";

    /**
     * 是否使用公平锁。
     *
     */
    boolean fairLock() default false;

    /**
     * 是否使用尝试锁。
     *
     */
    boolean tryLock() default false;

    /**
     * 等待时间
     *
     */
    long waitTime() default 30L;

    /**
     * 过期时间
     */
    long leaseTime() default 30L;

    /**
     * 时间粒度(默认为秒)
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

}
