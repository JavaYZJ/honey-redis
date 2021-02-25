# Honey-Redis
### 简介
####honey-redis 是基于Redis二次封装的一个中间件。目前里面封装实现了 Redis 五种基本数据类型的常用api、Redis全局唯一ID、Redis分布式、Redis Geo地理空间 等模块功能。

### 优缺点
####优点：基于springboot start 思想封装，开箱即用
####缺点：。。。

### 迭代信息
| 版本号  |  更新信息 |
| :------------ |:---------------:| 
| v1.0.0      | Redis 五种基本数据类型的常用api、Redis全局唯一ID、Redis分布式三模块       |   
| v1.1.0      | 新增Redis Geo 模块以及接口幂等性（请求重复判断）       | 



###1、引入依赖
```java  
    <!-- honey-redis-->
    <dependency>
        <groupId>red.honey</groupId>
        <artifactId>honey-redis</artifactId>
        <version>1.1.0</version>
    </dependency>
```
###2、在您的启动类上打上注解@EnableHoneyRedis
```java  
  @EnableHoneyRedis
  public class DemoApplication {
      public static void main(String[] args) {
          SpringApplication.run(DemoApplication.class, args);
      }
  }
```
###3、HoneyRedis封装实现了五种基本类型的api.借助IOC依赖注入即可使用.具体读者可自行阅读api
```java
    @Autowired
    private HoneyRedis honeyRedis; 
```
####3.1、HoneyRedis新特性--重复请求/并发请求处理。封装在HoneyRedis组件中
```java
    public boolean isReqDuplicate(String userId, String method, String deDuplicateParam)
  
    public boolean isReqDuplicate(String key)
```
###4、HoneyRedisId 封装实现了全局唯一ID,支持高并发、自定义业务前缀生成分布式唯一Id
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
###5、HoneyRedisLock 这是基于Spring Boot 实现 Redis 分布式锁,是可重入的.用法与Java的lock 基本一样.
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
###6、HoneyGeo 空间地理位置。新增空间位置出入库、空间位置坐标信息查询、空间位置距离计算、指定位置的附近空间位置查询等功能特性.
####6.1 空间位置入库.支持集合型批量加入。

**public void addGeoBean(String key, List<PositionInfo> positionInfos)**

| 参数  | 类型  | 描述 |
| :------------ |:---------------:| -----:|
| key      | java.lang.String | redis key |
| PositionInfo      | red.honey.redis.entiy.PositionInfo        |   位置信息（含位置名、经纬度） |
```java
    @Before
    public void init() {

        positionInfos = new ArrayList<>();
        positionInfos.add(new PositionInfo("合肥", 117.17, 31.52));
        positionInfos.add(new PositionInfo("安庆", 117.02, 30.31));
        positionInfos.add(new PositionInfo("淮北", 116.47, 33.57));
        positionInfos.add(new PositionInfo("宿州", 116.58, 33.38));
        positionInfos.add(new PositionInfo("阜阳", 115.48, 32.54));
        positionInfos.add(new PositionInfo("蚌埠", 117.21, 32.56));
        positionInfos.add(new PositionInfo("黄山", 118.18, 29.43));
    }

    @Test
    public void testAddGeo() {
        honeyGeo.addGeoBean("HB_GEO_KEY", positionInfos);
    }
```
####6.2 空间位置删除
**public void removeGeoBean(String key, Object... member)**

| 参数  | 类型  | 描述 |
| :------------ |:---------------:| -----:|
| key      | java.lang.String | redis key |
| member      | java.lang.Object       |   位置名 |
```java
    @Test
    public void testAddGeo() {
        honeyGeo.addGeoBean("HB_GEO_KEY", positionInfos);
    }
```
####6.3 根据位置名获取空间坐标
**public List<Point> getPosition(String key, String... member)** 

| 参数  | 类型  | 描述 |
| :------------ |:---------------:| -----:|
| key      | java.lang.String | redis key |
| member      | java.lang.Object       |   位置名 |
```java
    @Test
    public void testGet() {
        List<Point> position = honeyGeo.getPosition("HB_GEO_KEY", "合肥");
        System.out.println(JSON.toJSONString(position));
    }
```
####6.4 计算两个空间地点的距离
**public Distance distance(String key, String source, String dest, Metric metric)**

| 参数  | 类型  | 描述 |
| :------------ |:---------------:| -----:|
| key      | java.lang.String | redis key |
| source      | java.lang.String       |   始发点 |
| dest      | java.lang.String       |   目的点 |
| metric      | org.springframework.data.geo.Metric       |   单位（默认是千米），可不传 |
```java
    @Test
    public void testDistance() {
        Distance distance = honeyGeo.distance("HB_GEO_KEY", "合肥", "黄山", Metrics.KILOMETERS);
        System.out.println(JSON.toJSONString(distance));
    }
```
####6.5 以给定的经纬度为中心， 返回与中心的距离不超过给定最大距离的所有位置元素。
**public GeoResults<RedisGeoCommands.GeoLocation<Object>> near(String key, double longitude, double latitude, long radius, Metric metric,long limit)**

> 这个特性最为有用，可以做“附件的xxx”功能，像共享单车的附近的车、美团的附近的餐馆等等
>
| 参数  | 类型  | 描述 |
| :------------ |:---------------:| -----:|
| key      | java.lang.String | redis key |
| longitude      | double       |   经度 |
| dest      | double       |   纬度 |
| radius      | long       |   半径（最大距离） |
| metric      | org.springframework.data.geo.Metric       |   单位（默认是千米），可不传 |
| limit      | long       |   返回个数 |
```java
    @Test
    public void testNear(){
        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = honeyGeo.near("HB_GEO_KEY", 118.18, 29.43, 5, 5);
        System.out.println(JSON.toJSONString(results));
    }
```
####6.6 以给定的位置为中心， 返回与中心的距离不超过给定最大距离的所有位置元素。
**public GeoResults<RedisGeoCommands.GeoLocation<Object>> nearByPlace(String key, String member, long radius, Metric metric, long limit)**

| 参数  | 类型  | 描述 |
| :------------ |:---------------:| -----:|
| key      | java.lang.String | redis key |
| member      | java.lang.String       |   位置名 |
| radius      | long       |   半径（最大距离） |
| metric      | org.springframework.data.geo.Metric       |   单位（默认是千米），可不传 |
| limit      | long       |   返回个数 |
```java
    @Test
    public void testNearPlace(){
        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = honeyGeo.nearByPlace("HB_GEO_KEY", "合肥", 200, 5);
        System.out.println(JSON.toJSONString(results));
    }
```
####6.7 计算位置的hash
**public List<String> geoHash(String key, String... members)**

| 参数  | 类型  | 描述 |
| :------------ |:---------------:| -----:|
| key      | java.lang.String | redis key |
| member      | java.lang.String       |   位置名 |
```java
    @Test
    public void testHash(){
        List<String> list = honeyGeo.geoHash("HB_GEO_KEY", "黄山");
        System.out.println(JSON.toJSONString(list));
    }
```
####未完待续， thanks!




