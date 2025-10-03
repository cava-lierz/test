package com.mentara.controller;

import com.mentara.dto.ApiResponse;
import com.mentara.util.CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存管理控制器
 * 提供缓存查看和清理的API接口
 */
@RestController
@RequestMapping("/cache")
public class CacheController {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CacheUtils cacheUtils;

    /**
     * 获取所有缓存信息
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getCacheInfo() {
        Map<String, Object> cacheInfo = new HashMap<>();
        
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof org.springframework.cache.concurrent.ConcurrentMapCache) {
                org.springframework.cache.concurrent.ConcurrentMapCache concurrentMapCache = 
                    (org.springframework.cache.concurrent.ConcurrentMapCache) cache;
                cacheInfo.put(cacheName, concurrentMapCache.getNativeCache().size());
            }
        }
        
        return ApiResponse.success(cacheInfo);
    }

    /**
     * 清除指定缓存
     */
    @DeleteMapping("/{cacheName}")
    public ApiResponse<String> evictCache(@PathVariable String cacheName) {
        if (!cacheUtils.cacheExists(cacheName)) {
            return ApiResponse.error("400", "缓存不存在: " + cacheName);
        }
        
        cacheUtils.evictCache(cacheName);
        return ApiResponse.success("缓存已清除: " + cacheName);
    }

    /**
     * 清除所有缓存
     */
    @DeleteMapping("/all")
    public ApiResponse<String> evictAllCaches() {
        cacheUtils.evictAllCaches();
        return ApiResponse.success("所有缓存已清除");
    }

    /**
     * 获取缓存名称列表
     */
    @GetMapping("/names")
    public ApiResponse<Map<String, Object>> getCacheNames() {
        Map<String, Object> result = new HashMap<>();
        result.put("cacheNames", cacheUtils.getCacheNames());
        result.put("count", cacheUtils.getCacheNames().size());
        return ApiResponse.success(result);
    }
} 