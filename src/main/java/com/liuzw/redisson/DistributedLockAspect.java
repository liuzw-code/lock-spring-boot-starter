package com.liuzw.redisson;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static com.liuzw.redisson.DistributedLockConstant.*;

/**
 * 分布式锁Aop
 *
 * @author liuzw
 */

@Slf4j
@Aspect
public class DistributedLockAspect {


    private final IDistributedLock distributedLock;

    public DistributedLockAspect(IDistributedLock distributedLock) {
        this.distributedLock = distributedLock;
    }

    /**
     * 切入点
     */
    @Pointcut("@annotation(com.liuzw.redisson.DistributedLock)")
    public void distributedLockAspect() {
    }


    /**
     * 环绕执行方法，并为当前方法加锁
     */
    @Around("distributedLockAspect()")
    public Object invoke(ProceedingJoinPoint pjp) {
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

        //解析spel 参数表达式
        if (lockName.contains(PARAM_SPEL)) {
            lockName = ParamParser.parser(lockName, method, pjp.getArgs());
        }

        String applicationName = SpringUtil.getBean(Environment.class).getProperty(APPLICATION_NAME);
        return PREFIX_LOCK_NAME + applicationName + ":" + lockName;
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
        //等待时间
        Integer waitTime = annotation.waitTime();
        //失效时间
        Integer leaseTime = annotation.leaseTime();
        //时间粒度
        TimeUnit timeUnit = annotation.timeUnit();
        boolean flag;
        // 是否使用尝试锁
        if (tryLock) {
            flag = distributedLock.tryLock(lockName, waitTime, leaseTime, timeUnit, fairLock);
        } else {
            flag = distributedLock.lock(lockName, leaseTime, timeUnit, fairLock);
        }
        if (flag) {
            return proceed(pjp, lockName);
        }
        throw new DistributedLockException("未获取到分布式锁");
    }


    private Object proceed(final ProceedingJoinPoint pjp, String lockName) {
        Object val = null;
        try {
            val = pjp.proceed();
        } catch (Throwable throwable) {
            log.error("执行方法报错：{}", throwable.getMessage());
        } finally {
            //解锁
            distributedLock.unlock(lockName);
        }
        return val;
    }


}
