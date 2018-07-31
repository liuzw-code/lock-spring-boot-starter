package com.liuzw.redisson.thread;

import com.liuzw.redisson.IDistributedLock;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 守护线程
 *
 * @author liuzw
 * @date 2018/7/31 10:13
 **/
@Slf4j
public class DaemonThread extends Thread {

    /**
     * 失效时间
     */
    private Long leaseTime;

    /**
     * 锁的名字
     */
    private String lockName;

    /**
     *
     */
    private IDistributedLock distributedLock;

    /**
     * 标记线程是否结束
     */
    private volatile Boolean flag = true;


    public DaemonThread(Long leaseTime, String lockName, IDistributedLock distributedLock) {
        this.leaseTime = leaseTime;
        this.lockName = lockName;
        this.distributedLock = distributedLock;
    }

    /**
     * 停止正在运行的线程
     */
    public void stopThread() {
        this.flag = false;
    }

    @Override
    public void run() {
        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
        while(flag) {
            //记录当前进入方法的时间
            Long endTime = System.currentTimeMillis();
            //主线程方法运行的时间即将超过失效时间时，延长锁的失效时间
            if (endTime - startTime.get() + 3000 > leaseTime * 1000) {
                //延长锁的时间
                distributedLock.expire(lockName, leaseTime, TimeUnit.SECONDS);
                //重新记录方法开始时间
                startTime.set(System.currentTimeMillis());
            }
        }
        if (!flag) {
            log.info("-------DaemonThread 停止运行-----");
        }
    }
}
