package com.community.trendingserviceapi.dto.post.response;

import java.time.LocalDateTime;

public record TrendingPostResponse(
        Long postId,
        String nickname,
        String title,
        String previewContent,
        long likeCount,
        long viewCount,
        LocalDateTime createdAt
) {
}
