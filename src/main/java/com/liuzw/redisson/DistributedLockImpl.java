package com.liuzw.redisson;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 * @author liuzw
 */

@Slf4j
@Component
public class DistributedLockImpl implements IDistributedLock {

    /**
     * 默认等待时间
     */
    private static final Long DEFAULT_WAIT_TIME = 30L;
    /**
     * 默认超时时间
     */
    private static final Long DEFAULT_EXPIRED_TIME = 30L;
    /**
     * 时间粒度 单位默认为秒
     */
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void lock(String lockName, Boolean fairLock) {
        lock(lockName, DEFAULT_EXPIRED_TIME, DEFAULT_TIME_UNIT, fairLock);
    }

    @Override
    public void lock(String lockName, Long leaseTime, TimeUnit timeUnit, Boolean fairLock) {
        log.info("-------->[{}]获取锁。", lockName);
        RLock lock = getLock(lockName, fairLock);
        lock.lock(leaseTime, timeUnit);
    }

    @Override
    public Boolean tryLock(String lockName, Boolean fairLock) {
        return tryLock(lockName, DEFAULT_WAIT_TIME, DEFAULT_EXPIRED_TIME, DEFAULT_TIME_UNIT, fairLock);
    }

    @Override
    public Boolean tryLock(String lockName, Long waitTime, Long leaseTime, TimeUnit timeUnit, Boolean fairLock) {
        log.info("-------->[{}]获取尝试锁。", lockName);
        RLock lock = getLock(lockName, fairLock);
        try {
            //获取尝试锁
            return lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            log.error("获取尝试锁出现异常", e);
        } 
        return false;
    }

    @Override
    public void unlock(String lockName) {
        log.info("-------->[{}]解锁", lockName);
        RLock lock = redissonClient.getLock(lockName);
        lock.unlock();
    }

    @Override
    public Boolean expire(String lockName, Long leaseTime, TimeUnit timeUnit) {
        log.info("-------->[{}]延长锁失效时间", lockName);
        RLock lock = redissonClient.getLock(lockName);
        return lock.expire(leaseTime, timeUnit);

    }

    /**
     * 获取锁
     * @param lockName  锁的名字
     * @param fairLock  是否获取公平锁
     * @return          RLock
     */
    private RLock getLock(String lockName, Boolean fairLock) {
        //获取公平锁
        if (fairLock) {
            log.info("-------->[{}]获取公平锁", lockName);
            return redissonClient.getFairLock(lockName);
        } else {
            return redissonClient.getLock(lockName);
        }
    }

}
