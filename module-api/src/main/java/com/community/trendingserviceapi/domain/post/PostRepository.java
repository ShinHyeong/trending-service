package com.community.trendingserviceapi.domain.post;

import com.community.trendingserviceapi.dto.post.response.PostDetailResponse;
import com.community.trendingserviceapi.dto.post.response.TrendingPostResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(value = """
    SELECT p.post_id AS postId
    FROM post p
    WHERE p.created_at >= DATE_SUB(NOW(), INTERVAL 3 HOUR)
    ORDER BY (p.like_count + (0.05 * p.view_count) + (0.002 * CHAR_LENGTH(p.content)) + (UNIX_TIMESTAMP(p.created_at) / 60)) DESC, p.post_id DESC
    LIMIT :limit
    """,
    nativeQuery = true)
    List<Long> findTrendingPostIds(@Param("limit") int limit);

}
