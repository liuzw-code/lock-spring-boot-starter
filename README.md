# redisson-spring-boot-starter

### 基于SpringBoot + Redisson 实现分布式锁

### 简介
    1. 使用SpringBoot框架搭建
    2. 分布式锁选用redis实现。选择官方提供的redisson
    3. 结合spring aop使用，使用方便。

### 使用

1. 在使用之前首先还需要在自己的项目中添加一个配置类,主要是为了能够把jar中的配置信息加载进来
````
@Configuration
@ComponentScan(basePackages = "com.liuzw.redisson")
public class Config {
}
````

2. 接着在配置文件中加入redis的配置，和正常redis的配置一样。
````
spring:
  redis:
    port: 6379
    database: 0
    host: 127.0.0.1
    password: xxxxx
````
3. 然后使用的时候只需在方法上加入注解 `@DistributedLock`

````
@DistributedLock
public void test() {
	// 执行业务代码
}
````
或者在类中注入`IDistributedLock`

````
@Autowired
private IDistributedLock distributedLock;

public void test() {
	distributedLock.lock(...)
	try {
       // 执行业务代码    
       
    } catch (Exception e) {
       log.error("执行方法报错：", e);
    } finally {
     //解锁
     distributedLock.unlock(...);
    }
}
````

### 说明
    当一个线程拿到锁之后,如果执行业务逻辑比较耗时,时间长于锁失效的时间,这个时候如果锁失效了但是业务逻辑没有执行完,第二个线程拿到锁在执行该业务逻辑,显然是是不行的。
    这里我们模拟一个守护线程来为主线程的锁进行续时。当锁的失效时间快要到了的时候,但是业务逻辑没执行完，就为该锁延长失效时间
    
   注入`IDistributedLock`这种方式是没有加入线程的。如需使用则在上述方法中进行添加
 ````
 @Autowired
 private IDistributedLock distributedLock;
 
 public void test() {
 	 distributedLock.lock(...)
 	 
 	 DaemonThread thread = new DaemonThread(lockName, distributedLock);
     thread.start();
     try {
         // 执行业务代码    
        
     } catch (Exception e) {
         log.error("执行方法报错：", e);
     } finally {
         //停止线程
         thread.stopThread();
         //解锁
         distributedLock.unlock(lockName);
     }
 }
 ````  