package com.community.trendingserviceapi.service;

import com.community.trendingserviceapi.domain.post.PostLikeJdbcRepository;
import com.community.trendingserviceapi.domain.post.Post;
import com.community.trendingserviceapi.domain.post.PostRepository;
import com.community.trendingserviceapi.dto.post.request.PostCreateRequest;
import com.community.trendingserviceapi.dto.post.request.PostUpdateRequest;
import com.community.trendingserviceapi.dto.post.response.PostDetailResponse;
import com.community.trendingserviceapi.dto.post.response.TrendingPostResponse;
import com.community.trendingserviceapi.exception.PostAccessDeniedException;
import com.community.trendingserviceapi.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostLikeJdbcRepository postLikeRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final PostViewBufferPublisher postViewBufferPublisher;
    private final PostLikeBufferPublisher postLikeBufferPublisher;

    private static final int TRENDING_POST_LIMIT = 10;
    private static final String TRENDING_POSTS_CACHE_KEY = "trending:posts";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10); // 스케줄러 주기(5분)보다 길게 설정하여 캐시 공백 방지

    // 인기글 목록 갱신 API : 갱신하고 캐시에 올려둠
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional(readOnly = true)
    public void updateTrendingPosts() {
        List<Long> postIds = getTrendingPostIds(TRENDING_POST_LIMIT);

        // 3시간 내 작성된 게시글이 없는 경우 : 빈 배열을 캐싱하고 종료
        if (postIds.isEmpty()) {
            redisTemplate.opsForValue().set(TRENDING_POSTS_CACHE_KEY, "[]", CACHE_TTL);
            return;
        }

        List<TrendingPostResponse> previews = postRepository.findTrendingPostPreviews(postIds);

        String jsonString = objectMapper.writeValueAsString(previews);
        redisTemplate.opsForValue().set(TRENDING_POSTS_CACHE_KEY, jsonString, CACHE_TTL);
    }

    public List<TrendingPostResponse> getTrendingPosts() {
        String cachedJson = redisTemplate.opsForValue().get(TRENDING_POSTS_CACHE_KEY);

        if (cachedJson == null || cachedJson.isBlank()) {
            //서버 재시작 후에 다시 5분단위 스케줄러 기다리는거 방지
            updateTrendingPosts();
            cachedJson = redisTemplate.opsForValue().get(TRENDING_POSTS_CACHE_KEY);

            if (cachedJson == null || cachedJson.isBlank()) {
                return List.of();
            }
        }
        return objectMapper.readValue(
                cachedJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrendingPostResponse.class)
        );
    }

    // 게시글 상세 조회 API
    public PostDetailResponse getPost(Long postId, Long userId) {
        PostDetailResponse response = postRepository.findPostDetailById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        postViewBufferPublisher.enqueue(postId, userId);

        return response;
    }

    public void createPost(Long userId, PostCreateRequest request) {
        postRepository.save(new Post(userId, request));
    }

    @Transactional
    public void updatePost(Long postId, Long userId, PostUpdateRequest request) {
        Post post =  postRepository.findById(postId)
                .orElseThrow(()-> new PostNotFoundException(postId));

        if (!post.getUserId().equals(userId)) {
            throw new PostAccessDeniedException(postId, userId);
        }

        post.update(request);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.getUserId().equals(userId)) {
            throw new PostAccessDeniedException(postId, userId);
        }

        postRepository.deleteById(postId);
    }

    public boolean likePost(Long postId, Long userId) {
        if (postLikeRepository.insertLike(postId, userId) == 0) { //INSERT문 요청은 바로 DB에 반영하지만
            return false;   // 이미 누름 (이력 O) — 큐에 안 보냄
        }
        postLikeBufferPublisher.enqueue(postId, +1); //UPDATE문 요청은 큐에 보낸다.(정확한 post.like_count값은 나중에 천천히 업데이트)
        return true;
    }

    public boolean unlikePost(Long postId, Long userId) {
        if (postLikeRepository.deleteLike(postId, userId) == 0) {
            return false;   // 애초에 안 누름 (이력 X)
        }
        postLikeBufferPublisher.enqueue(postId, -1);
        return true;
    }

    private List<Long> getTrendingPostIds(int limit) {
        return postRepository.findTrendingPostIds(limit);
    }
}
