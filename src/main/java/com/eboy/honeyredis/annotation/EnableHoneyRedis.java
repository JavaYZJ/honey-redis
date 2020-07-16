package com.eboy.honeyredis.annotation;

import com.eboy.honeyredis.config.RedisAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author yangzhijie
 * @date 2020/7/16 9:57
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RedisAutoConfiguration.class)
public @interface EnableHoneyRedis {
}
