package com.campus.trade.cache;

import com.campus.trade.VO.ItemVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class ItemCache {

    private static final String KEY_PREFIX = "item:detail:";
    private static final String NULL_VALUE = "__NULL__";
    private static final Duration BASE_TTL = Duration.ofMinutes(10);
    private static final Duration NULL_TTL = Duration.ofSeconds(60);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    public CacheResult<ItemVO> get(Long itemId) {
        try {
            String json = stringRedisTemplate.opsForValue().get(KEY_PREFIX + itemId);
            if (json == null) {
                return CacheResult.miss();
            }
            if (NULL_VALUE.equals(json)) {
                return CacheResult.hit(null);
            }
            return CacheResult.hit(objectMapper.readValue(json, ItemVO.class));
        } catch (Exception e) {
            log.warn("读取商品缓存失败，降级查库，itemId={}", itemId, e);
            return CacheResult.miss();
        }
    }

    public void put(ItemVO vo) {
        try {
            Duration ttl = BASE_TTL.plusSeconds(ThreadLocalRandom.current().nextInt(0, 120));
            stringRedisTemplate.opsForValue().set(
                    KEY_PREFIX + vo.getId(),
                    objectMapper.writeValueAsString(vo),
                    ttl);
        } catch (Exception e) {
            log.warn("写入商品缓存失败，itemId={}", vo.getId(), e);
        }
    }

    public void putNull(Long itemId) {
        try {
            stringRedisTemplate.opsForValue().set(KEY_PREFIX + itemId, NULL_VALUE, NULL_TTL);
        } catch (Exception e) {
            log.warn("写入空值缓存失败，itemId={}", itemId, e);
        }
    }

    public void evict(Long itemId) {
        try {
            stringRedisTemplate.delete(KEY_PREFIX + itemId);
        } catch (Exception e) {
            log.warn("删除商品缓存失败, itemId={}", itemId, e);
        }
    }

    public record CacheResult<T>(boolean hit, T value) {
        public static <T> CacheResult<T> miss() {
            return new CacheResult<>(false, null);
        }

        public static <T> CacheResult<T> hit(T value) {
            return new CacheResult<>(true, value);
        }
    }
}