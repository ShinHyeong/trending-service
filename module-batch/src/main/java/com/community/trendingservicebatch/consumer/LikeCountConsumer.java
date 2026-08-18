package com.community.trendingservicebatch.consumer;
import com.community.trendingservicebatch.service.LikeCountService;
import com.community.trendingservicecore.dto.post.event.PostLikeBatchMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeCountConsumer {
    private final LikeCountService likeCountService;

    @SqsListener("${sqs.queue.like-count:post-like-queue}")
    public void consume(PostLikeBatchMessage message) {
        likeCountService.applyLikeCounts(message.events());
        log.debug("처리 완료: batchId={}", message.batchId());
    }

}
