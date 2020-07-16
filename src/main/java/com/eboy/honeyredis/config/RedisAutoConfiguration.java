package com.eboy.honeyredis.config;

import com.eboy.honeyredis.component.HoneyRedis;
import com.eboy.honeyredis.component.HoneyRedisId;
import com.eboy.honeyredis.component.HoneyRedisLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.redis.util.RedisLockRegistry;


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
