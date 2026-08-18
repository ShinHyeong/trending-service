package com.community.trendingservicebatch.service;

import com.community.trendingservicebatch.repository.PostJdbcRepository;
import com.community.trendingservicebatch.repository.PostViewJdbcRepository;
import com.community.trendingservicecore.dto.post.event.PostViewEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
public class ViewCountService {
    private final PostViewJdbcRepository postViewRepository;
    private final PostJdbcRepository postRepository;

    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void applyViewCounts(List<PostViewEvent> events) {
        if (events == null || events.isEmpty()) return;

        //한 메시지에 100개 이벤트가 들어있고, 그게 여러 postId에 걸쳐 있음 -> 컨슈머가 여러 개 돌 경우 데드락 발생 위험
        //해시맵으로 postId 오름차순 정렬
        Map<Long, List<Long>> userIdsByPost = events.stream()
                .distinct() // 새로고침 2번 누르기 등 중복 조회 발생 -> PK때문에 INSERT되진 않지만 쿼리 길이 길어져서 일단 최적화
                .collect(Collectors.groupingBy(
                        PostViewEvent::postId,
                        TreeMap::new,
                        Collectors.mapping(PostViewEvent::userId, Collectors.toList())));

        Map<Long, Integer> deltas = new LinkedHashMap<>();
        userIdsByPost.forEach((postId, userIds) -> {
            int newViews = postViewRepository.recordPostViews(postId, userIds);
            if (newViews > 0) deltas.put(postId, newViews);
        });

        postRepository.increaseViewCounts(deltas);
    }
}
