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
     * 失效时间  默认失效时间30s
     */
    private Long leaseTime = 30L;

    /**
     * 提前操作时间 默认3s
     */
    private Integer advanceTime = 3;

    /**
     * 锁的名字
     */
    private final String lockName;

    /**
     * 标记线程是否结束
     */
    private Boolean flag = true;

    /**
     * 守护线程开始执行时间
     */
    private Long startTime;

    /**
     * 分布式锁
     */
    private final IDistributedLock distributedLock;

    public DaemonThread(String lockName, IDistributedLock distributedLock) {
        this.startTime = System.currentTimeMillis();
        this.lockName = lockName;
        this.distributedLock = distributedLock;
    }

    public DaemonThread(Long leaseTime, String lockName, IDistributedLock distributedLock) {
        this.leaseTime = leaseTime;
        this.startTime = System.currentTimeMillis();
        this.lockName = lockName;
        this.distributedLock = distributedLock;
    }

    public DaemonThread(Long leaseTime, Integer advanceTime, String lockName, IDistributedLock distributedLock) {
        this.leaseTime = leaseTime;
        this.advanceTime = advanceTime;
        this.startTime = System.currentTimeMillis();
        this.lockName = lockName;
        this.distributedLock = distributedLock;
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
                // 主线程方法运行的时间即将超过失效时间时，延长锁的失效时间
                // 提前时间 写死了3秒 即在锁失效前3秒续航锁的失效时间
                // 此值可以根据具体的情况设置 在注解@DistributedLock 中advanceTime设置 单位为秒
                if ((endTime - startTime) / 1000 + advanceTime > leaseTime) {
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
    }
}
