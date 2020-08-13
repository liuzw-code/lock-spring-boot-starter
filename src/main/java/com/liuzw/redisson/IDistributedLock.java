package com.liuzw.redisson;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 *
 * @author liuzw
 */
public interface IDistributedLock {

    /**
     * 使用分布式锁
     *
     * @param lockName  锁的名字
     */
    void lock(String lockName);

    /**
     * 使用分布式锁，使用锁默认超时时间。
     * 
     * @param lockName  锁的名字
     * @param fairLock 是否使用公平锁
     */
    void lock(String lockName, Boolean fairLock);

    /**
     * 使用分布式锁。自定义锁的超时时间
     *
     * @param lockName  锁的名字
     * @param leaseTime 锁超时时间。超时后自动释放锁。
     * @param timeUnit  时间粒度
     * @param fairLock 是否使用公平锁
     */
    void lock(String lockName, Long leaseTime, TimeUnit timeUnit, Boolean fairLock);

    /**
     * 尝试分布式锁，使用锁默认等待时间、超时时间。
     *
     * @param lockName  锁的名字
     * @return Boolean
     */
    Boolean tryLock(String lockName);

    /**
     * 尝试分布式锁，使用锁默认等待时间、超时时间。
     * 
     * @param lockName  锁的名字
     * @param fairLock 是否使用公平锁
     * @return Boolean
     */
    Boolean tryLock(String lockName, Boolean fairLock);

    /**
     * 尝试分布式锁，自定义等待时间、超时时间。
     * 
     * @param lockName     锁的名字
     * @param waitTime     获取锁最长等待时间
     * @param leaseTime    锁超时时间。超时后自动释放锁。
     * @param timeUnit     时间粒度
     * @param fairLock     是否使用公平锁
     * @return Boolean
     */
    Boolean tryLock(String lockName, Long waitTime, Long leaseTime, TimeUnit timeUnit, Boolean fairLock);

    /**
     * 是否上锁
     *
     * @param lockName 锁的名字
     * @return Boolean
     */
    Boolean isLock(String lockName);

    /**
     * 关闭锁
     *
     * @param lockName 锁的名字
     */
    void unlock(String lockName);

    /**
     * 延长锁的时间
     *
     * @param lockName  锁的名字
     * @param leaseTime 失效时间
     * @param timeUnit  时间粒度
     * @return          Boolean
     */
    Boolean expire(String lockName, Long leaseTime, TimeUnit timeUnit);

}
