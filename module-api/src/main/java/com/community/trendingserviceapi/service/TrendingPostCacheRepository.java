package com.community.trendingserviceapi.service;

import com.community.trendingserviceapi.dto.post.response.TrendingPostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 인기글 JSON을 Redis에 String으로 저장하고 조회
 */
@Component
@RequiredArgsConstructor
public class TrendingPostCacheRepository {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // API 스펙을 바꿀 때마다 키 버전을 변경한다
    private static final String TRENDING_POSTS_CACHE_KEY = "trending:posts:v1";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10); // 스케줄러 주기(5분)보다 길게 설정하여 캐시 공백 방지

    // 역직렬화 없이 문자열 그대로
    public Optional<String> findJson() {
        String cachedJson = redisTemplate.opsForValue().get(TRENDING_POSTS_CACHE_KEY);
        return (cachedJson == null || cachedJson.isBlank())
                ? Optional.empty()
                : Optional.of(cachedJson);
    }

    public String save(List<TrendingPostResponse> posts) {
        String jsonString = objectMapper.writeValueAsString(posts);
        redisTemplate.opsForValue()
                .set(TRENDING_POSTS_CACHE_KEY, jsonString, CACHE_TTL);
        return jsonString;
    }
}
