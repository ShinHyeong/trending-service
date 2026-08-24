package com.community.trendingserviceapi.service;

import com.community.trendingservicecore.dto.post.event.PostViewBatchMessage;
import com.community.trendingservicecore.dto.post.event.PostViewEvent;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostViewBufferPublisher {
    private final SqsTemplate sqsTemplate;

    @Value("${sqs.queue.view-count:post-view-queue}")
    private String queueName;

    @Value("${instance.id:api-worker-local-01}")
    private String serverInstanceId;

    private static final int BATCH_SIZE = 100;
    private final ConcurrentLinkedQueue<PostViewEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger queueSize = new AtomicInteger(0);

    public void enqueue(Long postId, Long userId) {
        eventQueue.offer(new PostViewEvent(postId, userId));
        if (queueSize.incrementAndGet() >= BATCH_SIZE) {
            flush();
        }
    }

    @Scheduled(fixedDelay = 200)
    public synchronized void flush() {
        if (eventQueue.isEmpty()) {
            return;
        }

        List<PostViewEvent> batchEvents = new ArrayList<>();
        PostViewEvent event;
        while (batchEvents.size() < BATCH_SIZE && (event = eventQueue.poll()) != null) {
            batchEvents.add(event);
            queueSize.decrementAndGet();
        }

        if (batchEvents.isEmpty()) {
            return;
        }

        String batchId = "batch_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + "_" + UUID.randomUUID().toString().substring(0, 8);
        PostViewBatchMessage message = new PostViewBatchMessage(
                batchId,
                serverInstanceId,
                System.currentTimeMillis(),
                batchEvents
        );

        sqsTemplate.sendAsync(queueName, message)
                .exceptionally(ex -> {
                    log.error("SQS 발행 실패: batchId={}", batchId, ex);
                    return null;
                });
    }

    @PreDestroy
    public void onShutdown() {
        flush();
    }

}