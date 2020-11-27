package red.honey.redis.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.redis.util.RedisLockRegistry;
import red.honey.redis.component.HoneyRedis;
import red.honey.redis.component.HoneyRedisId;
import red.honey.redis.component.HoneyRedisLock;


/**
 * @author yangzhijie
 * @date 2020/7/16 9:39
 */
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnClass({HoneyRedis.class, HoneyRedisId.class, HoneyRedisLock.class})
@Import({RedisConfig.class, RedisLockConfig.class})
public class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HoneyRedis honeyRedis() {
        return new HoneyRedis();
    }

    @Bean
    @ConditionalOnMissingBean
    public HoneyRedisId honeyRedisId() {
        return new HoneyRedisId();
    }

    @Bean
    @ConditionalOnMissingBean
    public HoneyRedisLock honeyRedisLock(RedisLockRegistry redisLockRegistry) {
        return new HoneyRedisLock(redisLockRegistry);
    }
}
