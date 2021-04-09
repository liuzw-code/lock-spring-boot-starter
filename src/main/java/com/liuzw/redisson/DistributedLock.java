package com.liuzw.redisson;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 自定义分布式锁注解
 * 被该注解注解的方法表示该方法已被加上分布式锁
 *
 * @author liuzw
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 锁的名字
     *
     * 如果lockName可以确定，直接设置该属性，不写着默认为 当前包名 + 类名 + 方法名
     * 如果锁名字包含# 例如#user.id 则从参数中获取值
     */
    String lockName() default "";

    /**
     * 是否使用公平锁。
     */
    boolean fairLock() default false;

    /**
     * 是否使用尝试锁。
     */
    boolean tryLock() default true;

    /**
     * 等待时间
     */
    int waitTime() default 0;

    /**
     * 过期时间
     */
    int leaseTime() default 30000;

    /**
     * 时间粒度(默认为秒)
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

}
