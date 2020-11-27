# Honey-Redis
### 简介
####honey-redis 是基于Redis二次封装的一个中间件。目前里面封装实现了 Redis 五种基本数据类型的常用api、Redis全局唯一ID、Redis分布式锁 三个模块功能。

### 优缺点
####优点：基于springboot start 思想封装，开箱即用
####缺点：。。。

### 如何使用
####1、引入依赖
```java  
    <!-- honey-redis-->
    <dependency>
        <groupId>red.honey</groupId>
        <artifactId>honey-redis</artifactId>
        <version>1.0.0</version>
    </dependency>
```
####2、在您的启动类上打上注解@EnableHoneyRedis
```java  
  @EnableHoneyRedis
  public class DemoApplication {
      public static void main(String[] args) {
          SpringApplication.run(DemoApplication.class, args);
      }
  }
```
####3、HoneyRedis封装实现了五种基本类型的api.借助IOC依赖注入即可使用.具体读者可自行阅读api
```java
    @Autowired
    private HoneyRedis honeyRedis; 
```
####4、HoneyRedisId 封装实现了全局唯一ID,支持高并发、自定义业务前缀生成分布式唯一Id
```java
   @SpringBootTest
   @RunWith(SpringRunner.class)
   @Slf4j
   public class HoneyRedisTest {
       // 请求总数
       public static int clientTotal = 50000;
       // 同时并发执行的线程数
       public static int threadTotal = 50000;
       @Autowired
       private HoneyRedisId honeyRedisId;
      
       @Test
       public void test() throws InterruptedException {
           ExecutorService executorService = Executors.newCachedThreadPool();
           // 信号量，此处用于控制并发的线程数
           final Semaphore semaphore = new Semaphore(threadTotal);
           // 闭锁，可实现计数器递减
           final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
           final List<String> rs = new LinkedList<>();
           for (int i = 0; i < clientTotal; i++) {
               executorService.execute(() -> {
                   try {
                       // 执行此方法用于获取执行许可，当总计未释放的许可数不超过规定threadTotal时，
                       // 允许通行，否则线程阻塞等待，直到获取到许可。
                       semaphore.acquire();
                       String id = honeyRedisId.getGloballyUniqueId("honeyRedis");
                       // 此处可替代为插入数据库，uid建立唯一索引
                       xxxService.insert(id);
                       log.info(id);
                       rs.add(id);
                       // 释放许可
                       semaphore.release();
                   } catch (Exception e) {
                       log.warn(e.getMessage());
                   }
                   // 闭锁减一
                   countDownLatch.countDown();
               });
           }
           //线程阻塞，直到闭锁值为0时，阻塞才释放，继续往下执行
           countDownLatch.await();
           executorService.shutdown();
           // 如果你不想建立数据表来验证的话，可以考虑下面这个来测试是否会
           // 生成重复的id哦
           log.info("去重前个数：{}", rs.size());
           Map<String, Long> map = rs.stream().collect(Collectors.groupingBy(p -> p, Collectors.counting()));
           List<String> repeat = new ArrayList<>();
           map.keySet().forEach(key -> {
               if (map.get(key) > 1) {
                   repeat.add(key);
               }
           });
           log.info("重复个数：{}", repeat.size());
           log.info(JSON.toJSONString(repeat));
           log.info("去重后个数：{}", rs.size() - repeat.size());
       }
   }
```
####5、HoneyRedisLock 这是基于Spring Boot 实现 Redis 分布式锁,是可重入的.用法与Java的lock 基本一样.
```java
 @RestController
 @RequestMapping("/honeyRedis")
 @Slf4j
 public class HoneyRedisController {
     @Autowired
     private HoneyRedisLock honeyRedisLock;
     @GetMapping("/lock")
     @AuthSkip
     public String lock(@RequestParam("key") String key) {
         ExecutorService executorService = Executors.newCachedThreadPool();
         for (int i = 0; i < 10; i++) {
             executorService.execute(() -> {
                 boolean lock = honeyRedisLock.tryLock(key,4);
                 if (lock) {
                     try {
                         // 模拟处理业业务
                         log.info("我拿到锁了，进来执行业务处理");
                         Thread.sleep(1000L);
                     } catch (InterruptedException e) {
                         log.warn(e.getMessage());
                     }
                     log.info(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                     honeyRedisLock.unlock(key);
                 } else {
                     log.info("我拿不到锁，还等了4秒，还是拿不到锁，老子不干了");
                 }
             });
         }
         executorService.shutdown();
         return "SUCCESS";
     }
 }
```
####未完待续， thanks!
