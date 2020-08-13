
# redisson-spring-boot-starter

### 基于SpringBoot + Redisson 实现分布式锁
     写的不好的地方请轻喷,或者请各位大佬指导一下,不胜感激。

### 简介
   1. 使用SpringBoot框架搭建
   2. 分布式锁选用redis实现。选择官方提供的redisson
   3. 结合spring aop使用,使用方便。

### 配置

 支持3种模式: 单点,集群,哨兵

1. 单点模式：

````
spring:
    redis:
    port: 6379
    database: 0
    host: 127.0.0.1
    password: xxxxx
````
2. 集群模式：

````
spring:
   redis:
    cluster:
      nodes: 192.168.14.221:19001,192.168.14.221:19002,192.168.14.221:19003
      max-redirects: 5
    password: xxxxx
````

3. 哨兵模式：

````
spring:
   redis:
    sentinel:
      nodes: 192.168.14.221:19001,192.168.14.221:19002,192.168.14.221:19003
      master: xxxxx
    password: xxxxx
````


### 使用

 1. 使用的时候只需在方法上加入注解 `@DistributedLock`

注解介绍：
````
public @interface DistributedLock {

    /**
     * 锁的名字
     *
     * 如果lockName可以确定，直接设置该属性，不写着默认为 当前包名 + 类名 + 方法名
     */
    String lockName() default "";

    /**
     * 是否使用公平锁。
     */
    boolean fairLock() default false;

    /**
     * 是否尝试锁。
     */
    boolean tryLock() default false;

    /**
     * 等待时间
     */
    long waitTime() default 30L;

    /**
     * 过期时间
     */
    long leaseTime() default 30L;

    /**
     * 锁超时提前操作时间 续航使用 单位秒 用于守护线程
     */
    int advanceTime() default 3;

    /**
     * 时间粒度(默认为秒)
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

}
````

````
 @DistributedLock
 public void test() {
    // 执行业务代码
 }
````

2. 在类中注入`IDistributedLock`

````
 @Autowired
 private IDistributedLock distributedLock;

 public void test() {
    distributedLock.lock(lockName)
    try {
     // 执行业务代码
    } catch (Exception e) {
       log.error("执行方法报错：", e);
    } finally {
     //解锁
     distributedLock.unlock(lockName);
    }
}
````

或者

````
 public void test1() {
    try {
      if (distributedLock.tryLock(lockName)) {
          // 执行业务代码
       }
    } catch (Exception e) {
       log.error("执行方法报错：", e);
    } finally {
     //解锁
     distributedLock.unlock(lockName);
    }
}

````


### 说明

    当一个线程拿到锁之后,如果执行业务逻辑比较耗时,时间长于锁失效的时间,这个时候如果锁失效了但是业务逻辑没有执行完,第二个线程拿到锁在执行该业务逻辑,显然是是不行的。
    这里我们使用一个守护线程来为主线程的锁进行续航。当锁的失效时间快要到了的时候,但是业务逻辑没执行完，就为该锁延长失效时间


注入`IDistributedLock`这种方式是没有加入守护线程的。如需使用则在上述方法中进行添加

````
  @Autowired
  private IDistributedLock distributedLock;

  public Object test() {
      String lockName = "";
      DaemonThread thread = new DaemonThread(30L, lockName, distributedLock);
      thread.setDaemon(true);
      thread.start();
      try {
            //这里可以处理没有拿到锁的时候直接返回,而不是让请求一直等待阻塞
            if (distributedLock.tryLock(lockName, true)) {
                // 执行业务代码
            }
      } catch (Exception e) {
          log.error("执行方法报错：", e);
      } finally {
          //停止守护线程
          thread.stopThread();
          //解锁
          distributedLock.unlock(lockName);
      }
  }
````

### 守护线程

````
public void run() {
        log.info("--------守护线程开始执行-------");
        try {
            while (flag) {
                //记录当前进入方法的时间
                Long endTime = System.currentTimeMillis();
                //主线程方法运行的时间即将超过失效时间时，延长锁的失效时间
                //提前时间 写死了3秒 即在锁失效前3秒续航锁的失效时间
                // 此值可以根据具体的情况设置 在DistributedLock 注解中advanceTime设置 单位为秒
                if ((endTime - startTime) / 1000 + advanceTime > leaseTime) {
                    //延长锁的时间
                    distributedLock.expire(lockName, leaseTime, TimeUnit.SECONDS);
                    //重新记录方法开始时间
                    startTime = System.currentTimeMillis();
                }
                //睡眠100毫秒 主要是节省点cpu资源
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (InterruptedException e) {
            log.error("守护线程执行异常", e);
        }
    }
````