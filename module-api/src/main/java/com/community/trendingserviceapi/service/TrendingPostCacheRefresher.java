package com.community.trendingserviceapi.service;

import com.community.trendingserviceapi.domain.PostWithAccount;
import com.community.trendingserviceapi.domain.post.PostRepository;
import com.community.trendingserviceapi.dto.post.response.TrendingPostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * DB에서 인기글을 뽑고 DTO를 조립해 Redis 캐시를 갱신한다
 */
@Component
@RequiredArgsConstructor
public class TrendingPostCacheRefresher {
    private final PostRepository postRepository;
    private final PostAccountService postAccountService;
    private final TrendingPostCacheRepository trendingPostCacheRepository;

    private static final int TRENDING_POST_LIMIT = 10;

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional(readOnly = true)
    public String refresh() {
        List<Long> postIds = postRepository.findTrendingPostIds(TRENDING_POST_LIMIT);

        List<TrendingPostResponse> posts = postIds.isEmpty()
                ? List.of()
                : getTrendingPosts(postIds);

        return trendingPostCacheRepository.save(posts);
    }

    /**
     * 인기글 목록 조회 API는 스케줄러가 응답 DTO를 만들어 Redis에 캐싱하므로,
     * 다른 API와 달리 서비스 계층에서 DTO를 조립해 반환하였다.
     */
    private List<TrendingPostResponse> getTrendingPosts(List<Long> postIds) {
        List<PostWithAccount> postWithAccounts = postAccountService.getPostWithAccounts(postIds);
        return postWithAccounts.stream()
                .map(TrendingPostResponse::from)
                .toList();
    }
}
