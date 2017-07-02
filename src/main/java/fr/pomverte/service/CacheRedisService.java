package fr.pomverte.service;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CacheRedisService implements CacheService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void push(final String channel, final String message) {
        this.stringRedisTemplate.convertAndSend(channel, message);
    }

    @Override
    public void add(final String key, final String value) {
        this.stringRedisTemplate.opsForValue().set(key, value);
    }

    @Override
    public String get(final String key) {
        return this.stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public String read(final String queue) {
        return this.stringRedisTemplate.opsForList().leftPop(queue);
    }
}
