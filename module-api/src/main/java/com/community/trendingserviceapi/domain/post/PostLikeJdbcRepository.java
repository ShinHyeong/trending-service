package com.community.trendingserviceapi.domain.post;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostLikeJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     *  좋아요 이력 저장
     *  @return 1이면 신규 좋아요, 0이면 이미 누른 상태
     *  */
    public int insertLike(Long postId, Long userId) {
        return jdbcTemplate.update(
                "INSERT IGNORE INTO post_like (post_id, user_id) VALUES (?, ?)",
                postId, userId);
    }

    /** 좋아요 이력 삭제
    /** @return 1이면 실제 취소됨, 0이면 애초에 안 누른 상태
     */
    public int deleteLike(Long postId, Long userId) {
        return jdbcTemplate.update(
                "DELETE FROM post_like WHERE post_id = ? AND user_id = ?",
                postId, userId);
    }

}
