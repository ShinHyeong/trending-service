package com.community.trendingservicebatch.service;

import com.community.trendingservicebatch.repository.PostJdbcRepository;
import com.community.trendingservicecore.dto.post.event.PostLikeEvent;
import com.community.trendingservicecore.dto.post.event.PostViewEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeCountService {
    private final PostJdbcRepository postRepository;

    @Transactional
    public void applyLikeCounts(List<PostLikeEvent> events) {
        if (events == null || events.isEmpty()) return;

        // postId별 합산. TreeMap → 오름차순 처리로 데드락 방지
        Map<Long, Integer> deltas = events.stream()
                .collect(Collectors.groupingBy(
                        PostLikeEvent::postId,
                        TreeMap::new,
                        Collectors.summingInt(PostLikeEvent::delta)));

        deltas.values().removeIf(d -> d == 0);   // 눌렀다 취소 → 합 0 → UPDATE 스킵
        postRepository.increaseLikeCounts(deltas);
    }
}
