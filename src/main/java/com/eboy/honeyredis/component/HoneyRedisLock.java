package com.eboy.honeyredis.component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.redis.util.RedisLockRegistry;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * @author yangzhijie
 * @date 2020/7/16 11:28
 */
@AllArgsConstructor
@Slf4j
public final class HoneyRedisLock {

    private static final long DEFAULT_EXPIRE_UNUSED = 60000L;

    private final RedisLockRegistry redisLockRegistry;

    /**
     * 上锁
     *
     * @param lockKey 锁key
     * @deprecated since 1.0
     */
    public void lock(String lockKey) {
        obtainLock(lockKey).lock();
    }

    /**
     * 尝试上锁
     *
     * @param lockKey 锁key
     * @return 是否上锁成功
     */
    public boolean tryLock(String lockKey) {
        Lock lock = obtainLock(lockKey);
        return lock.tryLock();
    }

    /**
     * 在指定时间内尝试上锁
     *
     * @param lockKey 锁key
     * @param seconds 秒
     * @return 是否成功
     */
    public boolean tryLock(String lockKey, long seconds) {
        Lock lock = obtainLock(lockKey);
        try {
            return lock.tryLock(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁key
     */
    public void unlock(String lockKey) {
        try {
            Lock lock = obtainLock(lockKey);
            lock.unlock();
            redisLockRegistry.expireUnusedOlderThan(DEFAULT_EXPIRE_UNUSED);
        } catch (Exception e) {
            log.error("分布式锁 [{}] 释放异常", lockKey, e);
        }
    }

    /**
     * 释放锁
     *
     * @param lockKey       锁key
     * @param keyExpireTime key过期时间
     */
    public void unlock(String lockKey, long keyExpireTime) {
        try {
            Lock lock = obtainLock(lockKey);
            lock.unlock();
            redisLockRegistry.expireUnusedOlderThan(keyExpireTime);
        } catch (Exception e) {
            log.error("分布式锁 [{}] 释放异常", lockKey, e);
        }
    }

    private Lock obtainLock(String lockKey) {
        return redisLockRegistry.obtain(lockKey);
    }
}
