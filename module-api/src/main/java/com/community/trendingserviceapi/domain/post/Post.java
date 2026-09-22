package com.community.trendingserviceapi.domain.post;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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


    public Post(Long userId, String title, String content) {
        this.userId = userId;
        this.title = title;
        this.content = content;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
}
