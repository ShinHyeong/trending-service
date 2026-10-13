package com.community.trendingserviceapi.service;

import com.community.trendingserviceapi.dto.post.response.TrendingPostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 인기글 JSON을 Redis에 String으로 저장하고 조회
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrendingPostCacheRepository {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // API 스펙을 바꿀 때마다 키 버전을 변경한다
    private static final String TRENDING_POSTS_CACHE_KEY = "trending:posts:v1";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10); // 스케줄러 주기(5분)보다 길게 설정하여 캐시 공백 방지
    private static final TypeReference<List<TrendingPostResponse>> TRENDING_POSTS_TYPE = new TypeReference<>() {};


    public Optional<List<TrendingPostResponse>> find() {
        String cachedJson = redisTemplate.opsForValue().get(TRENDING_POSTS_CACHE_KEY);
        if (cachedJson == null || cachedJson.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(cachedJson, TRENDING_POSTS_TYPE));
        } catch (JacksonException e) {
            // 스펙 변경 후 키 버전을 깜박했거나 값이 꺠진 경우 -> 캐시 미스로 처리하고 재 생성
            log.warn("인기글 캐시 역직렬화 실패, 캐시 미스로 처리", e);
            return Optional.empty();
        }
    }

    public void save(List<TrendingPostResponse> posts) {
        redisTemplate.opsForValue()
                .set(TRENDING_POSTS_CACHE_KEY, objectMapper.writeValueAsString(posts), CACHE_TTL);
    }
}
