package com.community.trendingserviceapi.dto.post.response;

import com.community.trendingserviceapi.domain.PostWithAccount;
import com.community.trendingserviceapi.domain.post.Post;

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
    private static final int PREVIEW_LENGTH = 100;

    public static TrendingPostResponse from(PostWithAccount pa) {
        Post post = pa.post();
        return new TrendingPostResponse(
                post.getPostId(),
                pa.authorNickname(),
                post.getTitle(),
                toPreview(post.getContent()),
                post.getLikeCount(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }

    private static String toPreview(String content){
        if(content==null) { return ""; }
        return content.length() > PREVIEW_LENGTH
                ? content.substring(0, PREVIEW_LENGTH)
                : content;
    }
}
