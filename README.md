# redisson-spring-boot-starter


## 基于SpringBoot + Redisson 实现分布式锁


    1. 使用SpringBoot框架搭建
    2. 分布式锁选用redis实现。选择官方提供的redisson
    3. 结合spring aop使用,只需在方法上加入注解 `@DistributedLock` 
    ```
     @DistributedLock
     public void test() {
     
        // 执行业务代码
        
     }
    ``` 
    
    或者在类中注入 IDistributedLock
    
    ```
       @Autowired
       private IDistributedLock distributedLock;
       
       public void test() {
          distributedLock.lock(...)
          // 执行业务代码
          distributedLock.unLock(...)
       }
    ```
     
      
   
 ## 说明
 
      当一个线程拿到锁之后,如果执行业务逻辑比较耗时,时间长于锁失效的时间,这个时候如果锁失效了但是业务逻辑没有执行完,  
    第二个线程拿到锁在执行该业务逻辑,显然是是不行的。
      这里我们加入一个守护线程来为主线程的锁进行续时。当锁的失效时间快要到了的时候,但是业务逻辑没执行完，就为该锁延长失效时间      
    
   
 
    
     
