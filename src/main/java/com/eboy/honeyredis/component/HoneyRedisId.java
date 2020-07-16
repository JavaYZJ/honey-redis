package com.eboy.honeyredis.component;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author yangzhijie
 * @date 2020/7/16 11:26
 */
public final class HoneyRedisId {

    private final String GLOBALLY_UNIQUE_ID_KEY = "GLOBALLY_UNIQUE_ID_KEY";

    @Autowired
    private HoneyRedis honeyRedis;


    /**
     * 生成全局唯一Id
     *
     * @return 全局唯一Id
     */
    public String getGloballyUniqueId() {
        // 递增因子
        long delta = 1;
        Long incr = honeyRedis.incr(GLOBALLY_UNIQUE_ID_KEY, delta);
        return currentDateTime() + String.format("%1$05d", incr);
    }

    /**
     * 生成前缀的全局唯一id
     *
     * @param preFix 前缀
     * @return 前缀的全局唯一id
     */
    public String getGloballyUniqueId(String preFix) {
        return preFix + getGloballyUniqueId();
    }

    /**
     * 生成指定递增因子的含前缀的全局唯一id
     *
     * @param preFix 前缀
     * @param delta  递增因子
     * @return 指定递增因子的含前缀的全局唯一id
     */
    public String getGloballyUniqueId(String preFix, long delta) {
        Long incr = honeyRedis.incr(GLOBALLY_UNIQUE_ID_KEY, delta);
        return preFix + currentDateTime() + String.format("%1$05d", incr);
    }

    /**
     * 获取当前时间
     *
     * @return 当前时间str
     */
    private String currentDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return dateTimeFormatter.format(localDateTime);
    }
}
