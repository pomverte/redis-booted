package fr.pomverte.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;

@Component
public class ExpirationCacheCustomizer implements CacheManagerCustomizer {

    /** TTL : Time To Live */
    private long ttl;

    public ExpirationCacheCustomizer(@Value("${spring.redis.ttl}") Long ttl) {
        this.ttl = (ttl == null) ? -1 : ttl;
    }

    @Override
    public void customize(CacheManager cacheManager) {
        if (cacheManager instanceof RedisCacheManager) {
            RedisCacheManager redisCache = (RedisCacheManager) cacheManager;
            redisCache.setDefaultExpiration(ttl);
            //Map<String, Long> expiresMap = new ConcurrentHashMap<>();
            //expiresMap.put("users", 10L);
            //redisCache.setExpires(expiresMap);
        }
    }
}
