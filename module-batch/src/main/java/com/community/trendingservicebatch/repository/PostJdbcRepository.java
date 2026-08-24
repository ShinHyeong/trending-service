package com.community.trendingservicebatch.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PostJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public void increaseViewCounts(Map<Long, Integer> deltas) {
        if (deltas.isEmpty()) return;

        List<Object[]> args = deltas.entrySet().stream()
                .map(e -> new Object[]{ e.getValue(), e.getKey() })
                .toList();

        jdbcTemplate.batchUpdate(
                "UPDATE post SET view_count = view_count + ? WHERE post_id = ?", args);
    }

    public void increaseLikeCounts(Map<Long, Integer> deltas) {
        if (deltas.isEmpty()) return;

        List<Object[]> args = deltas.entrySet().stream()
                .map(e -> new Object[]{ e.getValue(), e.getKey() })
                .toList();

        jdbcTemplate.batchUpdate(
                "UPDATE post SET like_count = like_count + ? WHERE post_id = ?", args);
    }
}
