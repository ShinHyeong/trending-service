package com.community.trendingserviceapi.domain.post;

import com.community.trendingserviceapi.dto.post.request.PostCreateRequest;
import com.community.trendingserviceapi.dto.post.request.PostUpdateRequest;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert //빈 필드들이 Null로 덮어써지는 것을 막기 위해서
@DynamicUpdate //빈 필드들이 Null로 덮어써지는 것을 막기 위해서
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    private Long userId;

    @Column(nullable = false, length = 50)
    private String title;

    private String content;

    private long viewCount;
    private long likeCount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    public Post(Long userId, PostCreateRequest request) {
        this.userId = userId;
        this.title = request.title();
        this.content = request.content();
    }

    public void update(PostUpdateRequest request) {
        this.title = request.title();
        this.content = request.content();
        this.updatedAt = LocalDateTime.now();
    }
}
