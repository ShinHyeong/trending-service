package com.community.trendingservicebatch.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PostViewJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     *  조회 이력 저장
     * @return 처음 조회한 건수 (=INSERT 성공횟수, INSERT성공 시 반환값 1을 리턴하기 때문)
     * */
    public int recordPostViews(Long postId, List<Long> userIds) {
        String placeholders = userIds.stream()
                .map(u -> "(?,?)")
                .collect(Collectors.joining(","));

        return jdbcTemplate.update( //실제로 쿼리가 성공한 행의 개수
                "INSERT IGNORE INTO post_view (post_id, user_id) VALUES " + placeholders,
                ps -> {
                    int i = 1;
                    for (Long userId : userIds) {
                        ps.setLong(i++, postId);
                        ps.setLong(i++, userId);
                    }
                });
    }

}
