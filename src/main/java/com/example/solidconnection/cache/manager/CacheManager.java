package com.example.solidconnection.cache.manager;

public interface CacheManager {
    void put(String key, Object value, Long ttl);
    Object get(String key);
    void evict(String key);
    void evictUsingPrefix(String key);
}
