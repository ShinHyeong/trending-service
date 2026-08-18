package com.community.trendingserviceapi.controller;

import com.community.trendingserviceapi.dto.post.request.PostCreateRequest;
import com.community.trendingserviceapi.dto.post.response.PostDetailResponse;
import com.community.trendingserviceapi.dto.post.request.PostUpdateRequest;
import com.community.trendingserviceapi.dto.post.response.PostLikeResponse;
import com.community.trendingserviceapi.dto.post.response.TrendingPostPreviewResponse;
import com.community.trendingserviceapi.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/trending")
    public ResponseEntity<List<TrendingPostPreviewResponse>> getTrendingPostPreviews() {
        return ResponseEntity.ok().body(postService.getTrendingPostPreviews());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPost(@PathVariable("postId") Long postId,
                        //post_view 테이블에 INSERT(postId, userId) 작업하려면 필요한 정보라서 꼭 넣어야함
  // 인증 기능 개발 X, 부하 테스트와 성능 튜닝에 집중하고 싶어서, 부하테스트 툴이 userId값을 HTTP 헤더에 직접 꽂게 함
                                                      @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok().body(postService.getPost(postId, userId));
    }

    @PostMapping
    public ResponseEntity<Void> createPost(@RequestHeader("X-User-Id") Long userId,
                                           @RequestBody @Valid PostCreateRequest request) {
        postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(@PathVariable("postId") Long postId,
                                           @RequestHeader("X-User-Id") Long userId,
                                           @RequestBody @Valid PostUpdateRequest request) {
        postService.updatePost(postId, userId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable("postId") Long postId,
                                           @RequestHeader("X-User-Id") Long userId) {
        postService.deletePost(postId, userId);
        return  ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<PostLikeResponse> likePost(@PathVariable("postId") Long postId,
                                         @RequestHeader("X-User-Id") Long userId) {
        boolean isChanged = postService.likePost(postId, userId);
        return ResponseEntity.ok(new PostLikeResponse(postId, true, isChanged));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<PostLikeResponse> unlikePost(@PathVariable("postId") Long postId,
                                           @RequestHeader("X-User-Id") Long userId) {
        boolean isChanged = postService.unlikePost(postId, userId);
        return ResponseEntity.ok(new PostLikeResponse(postId, false, isChanged));
    }

}
