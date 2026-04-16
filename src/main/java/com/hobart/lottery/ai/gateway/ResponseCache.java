package com.hobart.lottery.ai.gateway;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

/**
 * 响应缓存 - 支持LRU驱逐和过期时间
 */
public class ResponseCache {

    private final int maxSize;
    private final Duration expireAfterWrite;
    private final ConcurrentHashMap<String, CacheEntry> cache;
    private final ReentrantLock writeLock = new ReentrantLock();

    public ResponseCache(int maxSize, Duration expireAfterWrite) {
        this.maxSize = maxSize;
        this.expireAfterWrite = expireAfterWrite;
        this.cache = new ConcurrentHashMap<>(maxSize * 2);
    }

    /**
     * 存储缓存条目
     */
    public void put(String key, String value) {
        writeLock.lock();
        try {
            // 驱逐过期和最老的条目
            evictExpired();
            
            if (cache.size() >= maxSize) {
                // 移除最老的条目（LRU）
                evictOldest();
            }
            
            CacheEntry entry = new CacheEntry(value, Instant.now().plus(expireAfterWrite));
            cache.put(key, entry);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 获取缓存条目
     * @return Optional包装的缓存值，如果不存在或已过期则返回空
     */
    public Optional<String> get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        
        // 检查是否过期
        if (entry.expiresAt().isBefore(Instant.now())) {
            cache.remove(key);
            return Optional.empty();
        }
        
        return Optional.of(entry.value());
    }

    /**
     * 驱逐所有过期条目
     */
    private void evictExpired() {
        Instant now = Instant.now();
        cache.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }

    /**
     * 驱逐最老的条目（LRU）
     */
    private void evictOldest() {
        if (cache.isEmpty()) {
            return;
        }
        
        // 找到最老的条目并移除
        Instant oldestTime = Instant.MAX;
        String oldestKey = null;
        
        for (java.util.Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            Instant createdAt = entry.getValue().createdAt();
            if (createdAt.isBefore(oldestTime)) {
                oldestTime = createdAt;
                oldestKey = entry.getKey();
            }
        }
        
        if (oldestKey != null) {
            cache.remove(oldestKey);
        }
    }

    /**
     * 缓存条目内部类
     */
    private static class CacheEntry {
        private final String value;
        private final Instant expiresAt;
        private final Instant createdAt;

        CacheEntry(String value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
            this.createdAt = Instant.now();
        }

        String value() {
            return value;
        }

        Instant expiresAt() {
            return expiresAt;
        }

        Instant createdAt() {
            return createdAt;
        }
    }
}