package com.community.trendingservicecore.dto.post.event;

public record PostLikeEvent(
        Long postId,
        int delta
) {
}
