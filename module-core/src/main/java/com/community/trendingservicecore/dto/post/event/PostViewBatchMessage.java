package com.community.trendingservicecore.dto.post.event;

import java.util.List;

public record PostViewBatchMessage(
        String batchId,
        String serverInstanceId,
        long createdAt,
        List<PostViewEvent> events
) {
}
