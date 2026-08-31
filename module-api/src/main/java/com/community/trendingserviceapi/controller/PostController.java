package com.community.trendingserviceapi.controller;

import com.community.trendingserviceapi.domain.post.Post;
import com.community.trendingserviceapi.dto.post.request.PostCreateRequest;
import com.community.trendingserviceapi.dto.post.request.PostUpdateRequest;
import com.community.trendingserviceapi.dto.post.response.ApiResponse;
import com.community.trendingserviceapi.dto.post.response.PostDetailResponse;
import com.community.trendingserviceapi.dto.post.response.PostLikeResponse;
import com.community.trendingserviceapi.dto.post.response.TrendingPostResponse;
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
    public ResponseEntity<ApiResponse<List<TrendingPostResponse>>> getTrendingPosts() {
        List<Post> posts = postService.getTrendingPosts();

        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();

        List<TrendingPostResponse> body = postService.getTrendingPosts();

        return ResponseEntity.ok(ApiResponse.success(body));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable("postId") Long postId,
                        //post_view 테이블에 INSERT(postId, userId) 작업하려면 필요한 정보라서 꼭 넣어야함
  // 인증 기능 개발 X, 부하 테스트와 성능 튜닝에 집중하고 싶어서, 부하테스트 툴이 userId값을 HTTP 헤더에 직접 꽂게 함
                                                      @RequestHeader("X-User-Id") Long userId) {
        PostDetailResponse body = postService.getPost(postId, userId);
        return ResponseEntity.ok(ApiResponse.success(body));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPost(@RequestHeader("X-User-Id") Long userId,
                                           @RequestBody @Valid PostCreateRequest request) {
        postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created());
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(@PathVariable("postId") Long postId,
                                           @RequestHeader("X-User-Id") Long userId,
                                           @RequestBody @Valid PostUpdateRequest request) {
        postService.updatePost(postId, userId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable("postId") Long postId,
                                           @RequestHeader("X-User-Id") Long userId) {
        postService.deletePost(postId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<PostLikeResponse>> likePost(@PathVariable("postId") Long postId,
                                         @RequestHeader("X-User-Id") Long userId) {
        boolean isChanged = postService.likePost(postId, userId);
        return ResponseEntity.ok(ApiResponse.success(new PostLikeResponse(postId, true, isChanged)));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<PostLikeResponse>> unlikePost(@PathVariable("postId") Long postId,
                                           @RequestHeader("X-User-Id") Long userId) {
        boolean isChanged = postService.unlikePost(postId, userId);
        return ResponseEntity.ok(ApiResponse.success(new PostLikeResponse(postId, false, isChanged)));
    }

}
