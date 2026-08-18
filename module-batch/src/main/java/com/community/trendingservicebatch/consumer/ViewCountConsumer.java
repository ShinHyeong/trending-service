package com.community.trendingservicebatch.consumer;
import com.community.trendingservicebatch.service.ViewCountService;
import com.community.trendingservicecore.dto.post.event.PostViewBatchMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountConsumer {
    private final ViewCountService viewCountService;

    @SqsListener("${sqs.queue.view-count:post-view-queue}")
    public void consume(PostViewBatchMessage message) {
        viewCountService.applyViewCounts(message.events());
        log.debug("처리 완료: batchId={}", message.batchId());
    }

}
