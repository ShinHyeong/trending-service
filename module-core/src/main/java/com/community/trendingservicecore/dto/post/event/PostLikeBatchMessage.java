package com.community.trendingservicecore.dto.post.event;

import java.util.List;

public record PostLikeBatchMessage(
        String batchId,
        String serverInstanceId,
        long createdAt,
        List<PostLikeEvent> events
) {
}
