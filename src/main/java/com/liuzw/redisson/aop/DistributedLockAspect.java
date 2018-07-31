package com.liuzw.redisson.aop;

import com.liuzw.redisson.IDistributedLock;
import com.liuzw.redisson.annotation.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 分布式锁Aop
 *
 * @author liuzw
 */

@Slf4j
@Aspect
@Component
public class DistributedLockAspect {

    /**
     * redis锁的前缀
     */
    private static final String PREFIX_LOCK_NAME = "redis:distributed:lock:";

    @Autowired
    private IDistributedLock distributedLock;

    /**
     * 切入点
     */
    @Pointcut("@annotation(com.liuzw.redisson.annotation.DistributedLock)")
    public void distributedLockAspect(){}


    /**
     * 环绕执行方法，并为当前方法加锁
     */
    @Around("distributedLockAspect()")
    public Object invoke(ProceedingJoinPoint pjp) {
        log.info("---------------->进入切面，开始获取分布式锁");
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();

        Method targetMethod = methodSignature.getMethod();
        //获取锁的名字
        final String lockName = getLockName(pjp, targetMethod);
        //获取锁，并执行相应的业务逻辑
        return lock(pjp, targetMethod, lockName);

    }


    /**
     * 获取锁的名字
     */
    private String getLockName(ProceedingJoinPoint pjp, Method method) {
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);
        //锁的名字
        String lockName = annotation.lockName();
        if (StringUtils.isEmpty(lockName)) {
            //切点所在的类名
            String className = pjp.getTarget().getClass().getName();
            //使用了注解的方法
            String methodName = pjp.getSignature().getName();
            //锁的名字
            lockName = className + ":" + methodName;
        }
        return PREFIX_LOCK_NAME + lockName;
    }


    /**
     * 获取锁，并执行相应的业务逻辑
     */
    private Object lock(ProceedingJoinPoint pjp, Method method, final String lockName) {
        //获取方法上的注解
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);
        //是否公平锁
        boolean fairLock = annotation.fairLock();
        //是否尝试锁
        boolean tryLock = annotation.tryLock();
        // 是否使用尝试锁
        if (tryLock) {
            return tryLock(pjp, annotation, lockName, fairLock);
        } else {
            return lock(pjp, annotation.leaseTime(), lockName, fairLock);
        }
    }

    /**
     * 普通锁方法执行
     */
    private Object lock(final ProceedingJoinPoint pjp, final Long leaseTime, final String lockName, boolean fairLock) {
        //获取锁
        distributedLock.lock(lockName,leaseTime, TimeUnit.SECONDS, fairLock);
        return proceed(pjp, leaseTime, lockName);
    }

    /**
     * 尝试锁方法执行
     */
    private Object tryLock(final ProceedingJoinPoint pjp, DistributedLock annotation, final String lockName, boolean fairLock) {
        //等待时间
        Long waitTime = annotation.waitTime();
        //失效时间
        Long leaseTime = annotation.leaseTime();
        //时间粒度
        TimeUnit timeUnit = annotation.timeUnit();
        //判断是否获取到锁
        Boolean flag = distributedLock.tryLock(lockName, waitTime, leaseTime, timeUnit, fairLock);
        //是否拿到锁
        if (flag) {
            return proceed(pjp, leaseTime, lockName);
        }
        return null;

    }


    /**
     * 执行业务逻辑
     */
    private Object proceed(final ProceedingJoinPoint pjp, Long leaseTime, String lockName) {
        Object val = null;
        try {
            expandTime(leaseTime, lockName);
            val = pjp.proceed();
        } catch (Throwable throwable) {
            log.error("执行方法报错：", throwable.getMessage());
        } finally {
            distributedLock.unlock(lockName);
        }
        return val;
    }

    /**
     * 为了防止线程在拿到锁之后，执行方法的时候，执行时间过长，
     * 超过了锁的失效时间而导致另一个线程拿到锁在执行该方法
     * 因此这里添加一个守护线程来为当前拿到锁的线程续时。
     *
     * @param leaseTime  锁失效时间
     * @param lockName   锁的名字
     */
    private void expandTime(Long leaseTime, String lockName) {
        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
        Thread thread = new Thread(() -> {
            while(true) {
                //记录当前进入方法的时间
                Long endTime = System.currentTimeMillis();
                //主线程方法运行的时间即将超过失效时间时，延长锁的失效时间
                if (endTime - startTime.get() + 2000 > leaseTime * 1000) {
                    //延长锁的时间
                    distributedLock.expire(lockName, leaseTime, TimeUnit.SECONDS);
                    //重新记录方法开始时间
                    startTime.set(System.currentTimeMillis());
                }
            }
        });
        //设置为 true 表示当前线程为守护线程
        thread.setDaemon(true);
        thread.start();
    }

}