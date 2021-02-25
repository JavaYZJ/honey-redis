package red.honey.redis.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import red.honey.redis.entiy.PositionInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yangzhijie
 * @date 2021/2/25 15:17
 */
public class HoneyGeo {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 添加geo信息
     *
     * @param key           缓存key
     * @param positionInfos 位置
     */
    public void addGeoBean(String key, List<PositionInfo> positionInfos) {
        Set<RedisGeoCommands.GeoLocation<Object>> locations = new HashSet<>();
        positionInfos.forEach(ci -> locations.add(new RedisGeoCommands.GeoLocation<Object>(
                ci.getMember(), new Point(ci.getLongitude(), ci.getLatitude())
        )));
        redisTemplate.opsForGeo().add(key, locations);
    }

    /**
     * 删除geo信息
     *
     * @param key    缓存key
     * @param member 位置名
     */
    public void removeGeoBean(String key, Object... member) {
        redisTemplate.opsForZSet().remove(key, member);
    }

    /**
     * 获取某(多)个地方的坐标
     *
     * @param key    缓存key
     * @param member 位置名
     * @return 坐标位置集合
     */
    public List<Point> getPosition(String key, String... member) {
        return redisTemplate.opsForGeo().position(key, member);
    }


    /**
     * 计算两个地点的距离
     *
     * @param key    缓存key
     * @param source 始发点
     * @param dest   目的点
     * @param metric 单位
     * @return 距离
     */
    public Distance distance(String key, String source, String dest, Metric metric) {
        return redisTemplate.opsForGeo().distance(key, source, dest, metric);
    }

    /**
     * 计算两个地点的距离
     *
     * @param key    缓存key
     * @param source 始发点
     * @param dest   目的点
     * @return 距离
     */
    public Distance distance(String key, String source, String dest) {
        return redisTemplate.opsForGeo().distance(key, source, dest);
    }

    /**
     * 以给定的经纬度为中心， 返回与中心的距离不超过给定最大距离的所有位置元素。
     *
     * @param key       缓存key
     * @param longitude 经度
     * @param latitude  纬度
     * @param radius    半径
     * @param limit     个数
     * @return 返回与中心的距离不超过给定最大距离的所有位置元素。
     */
    public GeoResults<RedisGeoCommands.GeoLocation<Object>> near(String key, double longitude, double latitude, long radius, long limit) {
        return near(key, longitude, latitude, radius, null, limit);
    }

    /**
     * 以给定的经纬度为中心， 返回与中心的距离不超过给定最大距离的所有位置元素。
     *
     * @param key       缓存key
     * @param longitude 经度
     * @param latitude  纬度
     * @param radius    半径
     * @param limit     个数
     * @return 返回与中心的距离不超过给定最大距离的所有位置元素。
     */
    public GeoResults<RedisGeoCommands.GeoLocation<Object>> near(String key, double longitude, double latitude, long radius, Metric metric, long limit) {
        if (metric == null) {
            metric = Metrics.KILOMETERS;
        }
        Circle circle = new Circle(longitude, latitude, radius * metric.getMultiplier());
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending()
                .limit(limit);

        return redisTemplate.opsForGeo()
                .radius(key, circle, args);
    }

    /**
     * 以给定的位置为中心， 返回与中心的距离不超过给定最大距离的所有位置元素。
     *
     * @param key    缓存key
     * @param member 位置名
     * @param radius 半径
     * @param limit  个数
     * @return 返回与中心的距离不超过给定最大距离的所有位置元素。
     */
    public GeoResults<RedisGeoCommands.GeoLocation<Object>> nearByPlace(String key, String member, long radius, long limit) {
        return nearByPlace(key, member, radius, null, limit);

    }

    /**
     * 以给定的位置为中心， 返回与中心的距离不超过给定最大距离的所有位置元素。
     *
     * @param key    缓存key
     * @param member 位置名
     * @param radius 半径
     * @param limit  个数
     * @return 返回与中心的距离不超过给定最大距离的所有位置元素。
     */
    public GeoResults<RedisGeoCommands.GeoLocation<Object>> nearByPlace(String key, String member, long radius, Metric metric, long limit) {
        if (metric == null) {
            metric = Metrics.KILOMETERS;
        }
        Distance distance = new Distance(radius, metric);
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending()
                .limit(limit);

        return redisTemplate.opsForGeo()
                .radius(key, member, distance, args);

    }

    /**
     * 计算位置的hash
     *
     * @param key     缓存key
     * @param members 位置
     * @return hash值
     */
    public List<String> geoHash(String key, String... members) {
        return redisTemplate.opsForGeo()
                .hash(key, members);
    }
}
