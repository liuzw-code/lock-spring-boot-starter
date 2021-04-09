package com.liuzw.redisson;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 *
 * @author liuzw
 */

@Slf4j
public class DefaultDistributedLock implements IDistributedLock {

    private final RedissonClient redissonClient;

    private final RedissonConfigProperties properties;


    public DefaultDistributedLock(RedissonClient redissonClient, RedissonConfigProperties properties) {
        this.redissonClient = redissonClient;
        this.properties = properties;
    }

    @Override
    public Boolean lock(String lockName) {
        return lock(lockName, properties.getLeaseTime(), properties.getTimeUnit(), true);
    }

    @Override
    public Boolean lock(String lockName, Boolean fairLock) {
        return lock(lockName, properties.getLeaseTime(), properties.getTimeUnit(), fairLock);
    }

    @Override
    public Boolean lock(String lockName, Integer leaseTime, TimeUnit timeUnit, Boolean fairLock) {
        RLock lock = getLock(lockName, fairLock);
        lock.lock(leaseTime, timeUnit);
        return Boolean.TRUE;
    }

    @Override
    public Boolean tryLock(String lockName) {
        return tryLock(lockName, properties.getWaitTime(), properties.getLeaseTime(), properties.getTimeUnit(), false);
    }

    @Override
    public Boolean tryLock(String lockName, Boolean fairLock) {
        return tryLock(lockName, properties.getWaitTime(), properties.getLeaseTime(), properties.getTimeUnit(), fairLock);
    }


    @Override
    public Boolean tryLock(String lockName, Integer waitTime, Integer leaseTime, TimeUnit timeUnit, Boolean fairLock) {
        RLock lock = getLock(lockName, fairLock);
        try {
            boolean flag = lock.tryLock(waitTime, leaseTime, timeUnit);
            log.info("-------->tryLock[key={}获取锁{}]", lockName, flag ? "成功" : "失败");
            return flag;
        } catch (Exception e) {
            log.error("获取尝试锁出现异常", e);
        }
        return false;
    }

    @Override
    public Boolean isLock(String lockName) {
        RLock lock = getLock(lockName, false);
        Boolean flag = lock.isLocked();
        log.info("-------->检测到key[{}]" + (flag ? "已" : "未") + "上锁", lockName);
        return flag;
    }

    @Override
    public void unlock(String lockName) {
        RLock lock = getLock(lockName, false);
        if (lock.isLocked()) {
            log.info("-------->[{}]解锁", lockName);
            lock.unlock();
        }
    }

    /**
     * 获取锁
     *
     * @param lockName 锁的名字
     * @param fairLock 是否获取公平锁
     * @return RLock
     */
    private RLock getLock(String lockName, Boolean fairLock) {
        if (fairLock) {
            return redissonClient.getFairLock(lockName);
        } else {
            return redissonClient.getLock(lockName);
        }
    }

}
