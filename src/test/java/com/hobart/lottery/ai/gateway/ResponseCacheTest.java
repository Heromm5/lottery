package com.hobart.lottery.ai.gateway;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 响应缓存测试类
 */
class ResponseCacheTest {

    @Test
    void shouldReturnCachedResponse() {
        // 测试：缓存应该返回存储的值
        ResponseCache cache = new ResponseCache(100, Duration.ofMinutes(10));
        String key = "predict:100:30";
        String expected = "cached_result";

        cache.put(key, expected);
        Optional<String> result = cache.get(key);

        assertTrue(result.isPresent(), "缓存应该返回存储的值");
        assertEquals(expected, result.get(), "返回的值应该与存储的值一致");
    }

    @Test
    void shouldReturnEmptyForMiss() {
        // 测试：不存在的key应该返回空
        ResponseCache cache = new ResponseCache(100, Duration.ofMinutes(10));
        Optional<String> result = cache.get("nonexistent");

        assertFalse(result.isPresent(), "不存在的key应该返回空Optional");
    }

    @Test
    void shouldEvictOldEntries() throws InterruptedException {
        // 测试：过期条目应该被驱逐
        ResponseCache cache = new ResponseCache(100, Duration.ofMillis(200));
        String key = "key1";
        String value = "value1";

        cache.put(key, value);

        // 等待过期
        Thread.sleep(300);

        Optional<String> result = cache.get(key);
        assertFalse(result.isPresent(), "过期的条目应该被驱逐");
    }

    @Test
    void shouldEvictOnSizeLimit() {
        // 测试：达到最大size时应该触发LRU驱逐
        ResponseCache cache = new ResponseCache(2, Duration.ofMinutes(10));
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3"); // 应该驱逐 key1

        assertFalse(cache.get("key1").isPresent(), "最老的条目key1应该被驱逐");
        assertTrue(cache.get("key2").isPresent(), "key2应该存在");
        assertTrue(cache.get("key3").isPresent(), "key3应该存在");
    }

    @Test
    void shouldStoreDifferentTypes() {
        // 测试：缓存应该能存储任意字符串值
        ResponseCache cache = new ResponseCache(100, Duration.ofMinutes(10));

        // 测试存储JSON字符串
        cache.put("json_key", "{\"result\":true}");
        Optional<String> jsonResult = cache.get("json_key");
        assertTrue(jsonResult.isPresent(), "JSON字符串应该被正确存储");
        assertEquals("{\"result\":true}", jsonResult.get());

        // 测试存储空字符串
        cache.put("empty_key", "");
        Optional<String> emptyResult = cache.get("empty_key");
        assertTrue(emptyResult.isPresent(), "空字符串应该被正确存储");
        assertEquals("", emptyResult.get());

        // 测试存储长字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("x");
        }
        String longValue = sb.toString();
        cache.put("long_key", longValue);
        Optional<String> longResult = cache.get("long_key");
        assertTrue(longResult.isPresent(), "长字符串应该被正确存储");
        assertEquals(longValue, longResult.get());
    }
}