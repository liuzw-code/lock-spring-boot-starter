package com.liuzw.redisson.thread;

import com.liuzw.redisson.IDistributedLock;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

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
    private final Long leaseTime;

    /**
     * 锁的名字
     */
    private final String lockName;

    /**
     * 分布式锁
     */
    private final IDistributedLock distributedLock;

    /**
     * 标记线程是否结束
     */
    private Boolean flag;

    /**
     * 守护线程开始执行时间
     */
    private Long startTime;


    public DaemonThread(String lockName, IDistributedLock distributedLock) {
        //默认失效时间30s
        this.leaseTime = 30L;
        this.lockName = lockName;
        this.distributedLock = distributedLock;
        this.flag = true;
        this.startTime = System.currentTimeMillis();
    }


    public DaemonThread(Long leaseTime, String lockName, IDistributedLock distributedLock) {
        this.leaseTime = leaseTime;
        this.lockName = lockName;
        this.distributedLock = distributedLock;
        this.flag = true;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 停止正在运行的线程
     */
    public void stopThread() {
        log.info("-----------关闭守护线程线程-------");
        this.flag = false;
    }

    @Override
    public void run() {
        log.info("--------守护线程开始执行-------");
        try {
            while (flag) {
                //记录当前进入方法的时间
                Long endTime = System.currentTimeMillis();
                //主线程方法运行的时间即将超过失效时间时，延长锁的失效时间
                //提前时间 写死了2秒 即在锁失效前2秒续航锁的失效时间
                int advanceTime = 2;
                if ((endTime - startTime) / 1000 + advanceTime > leaseTime) {
                    log.info("--------开始延长锁的时间-------");
                    //延长锁的时间
                    distributedLock.expire(lockName, leaseTime, TimeUnit.SECONDS);
                    //重新记录方法开始时间
                    startTime = System.currentTimeMillis();
                }
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (InterruptedException e) {
            log.error("守护线程执行异常", e);
        }
        log.info("-------DaemonThread 停止运行-----");
    }
}
