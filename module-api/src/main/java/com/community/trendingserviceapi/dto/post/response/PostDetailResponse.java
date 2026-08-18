package com.community.trendingserviceapi.dto.post.response;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long postId,
        String nickname,
        String title,
        String content,
        long likeCount,
        long viewCount,
        LocalDateTime createdAt
) {
}
