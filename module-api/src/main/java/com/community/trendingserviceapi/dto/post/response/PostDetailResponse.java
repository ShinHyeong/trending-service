package com.community.trendingserviceapi.dto.post.response;

import com.community.trendingserviceapi.domain.PostWithAccount;
import com.community.trendingserviceapi.domain.post.Post;

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
    public static PostDetailResponse from(PostWithAccount pa) {
        Post post = pa.post();
        return new PostDetailResponse(
                post.getPostId(),
                pa.authorNickname(),
                post.getTitle(),
                post.getContent(),
                post.getLikeCount(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}
