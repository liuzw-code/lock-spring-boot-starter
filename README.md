# lock-spring-boot-starter

### 基于SpringBoot + Redisson 实现分布式锁

### 简介
   1. 使用SpringBoot框架搭建
   2. 分布式锁选用redis实现。选择官方提供的redisson
   3. 结合spring aop使用,使用方便。

### 配置

 组件默认使用spring原生redis配置

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


1. 使用的时候只需在方法上加入注解 `@DistributedLock`即可,注解具体参数自行设置

````
 @DistributedLock
 public void test() {
    // 执行业务代码
 }
````

如果需要从参数中获取锁的key

````
 @DistributedLock(lockName="#user.id")
 public void test(@RequestBody User user) {
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
