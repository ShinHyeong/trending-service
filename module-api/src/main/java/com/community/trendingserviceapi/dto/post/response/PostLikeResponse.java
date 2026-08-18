package com.community.trendingserviceapi.dto.post.response;

public record PostLikeResponse(
        Long postId,
        boolean isLiked, // 현재 상태 (요청 후)
        boolean isChanged // 요청으로 실제로 변경되었는지 (모니터링용도)

) {
}
