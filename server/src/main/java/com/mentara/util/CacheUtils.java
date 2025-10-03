package com.mentara.util;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 缓存工具类
 * 提供常用的缓存操作方法
 */
@Component
public class CacheUtils {

    private final CacheManager cacheManager;

    public CacheUtils(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * 清除指定缓存
     * @param cacheName 缓存名称
     */
    public void evictCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * 清除指定缓存中的特定键
     * @param cacheName 缓存名称
     * @param key 缓存键
     */
    public void evictCache(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    /**
     * 清除所有缓存
     */
    public void evictAllCaches() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            evictCache(cacheName);
        }
    }

    /**
     * 获取缓存统计信息
     * @param cacheName 缓存名称
     * @return 缓存对象，可能为null
     */
    public Cache getCache(String cacheName) {
        return cacheManager.getCache(cacheName);
    }

    /**
     * 获取所有缓存名称
     * @return 缓存名称集合
     */
    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }

    /**
     * 检查缓存是否存在
     * @param cacheName 缓存名称
     * @return 是否存在
     */
    public boolean cacheExists(String cacheName) {
        return cacheManager.getCache(cacheName) != null;
    }
} 