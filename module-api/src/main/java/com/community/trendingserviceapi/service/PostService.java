package com.community.trendingserviceapi.service;

import com.community.trendingserviceapi.domain.post.Post;
import com.community.trendingserviceapi.domain.post.PostRepository;
import com.community.trendingserviceapi.dto.post.request.PostCreateRequest;
import com.community.trendingserviceapi.dto.post.request.PostUpdateRequest;
import com.community.trendingserviceapi.dto.post.response.PostDetailResponse;
import com.community.trendingserviceapi.dto.post.response.TrendingPostPreviewResponse;
import com.community.trendingserviceapi.dto.post.response.TrendingPostProjection;
import com.community.trendingserviceapi.exception.PostAccessDeniedException;
import com.community.trendingserviceapi.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    private static final int TRENDING_POST_LIMIT = 10;

    // 인기글 목록 조회 API
    @Transactional(readOnly = true)
    public List<TrendingPostPreviewResponse> getTrendingPostPreviews() {
        List<TrendingPostProjection> trendingPosts = getTrendingPosts(TRENDING_POST_LIMIT);

        List<Long> trendingPostIds = new ArrayList<>();
        for (TrendingPostProjection trendingPost : trendingPosts) {
            trendingPostIds.add(trendingPost.getPostId());
        }

        return postRepository.findTrendingPostPreviews(trendingPostIds);
    }

    // 게시글 상세 조회 API
    public PostDetailResponse getPost(Long postId, Long userId) {
        //조회수 처리 - SQS로 메세지 전송
        //사용자에게는 게시글 상세정보 리턴
        return postRepository.findPostDetailById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    // 게시글 생성 API
    public void createPost(Long userId, PostCreateRequest request) {
        postRepository.save(new Post(userId, request));
    }

    // 게시글 수정 API
    @Transactional
    public void updatePost(Long postId, Long userId, PostUpdateRequest request) {
        Post post =  postRepository.findById(postId)
                .orElseThrow(()-> new PostNotFoundException(postId));

        if (!post.getUserId().equals(userId)) {
            throw new PostAccessDeniedException(postId, userId);
        }

        post.update(request);
    }

    // 게시글 삭제 API
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.getUserId().equals(userId)) {
            throw new PostAccessDeniedException(postId, userId);
        }

        postRepository.deleteById(postId);
    }

    public void likePost(Long postId, Long userId) {
        //SQS 서버에게 메세지 발행
    }

    // 게시글 좋아요 삭제 API
    public void unlikePost(Long postId, Long userId) {
        //SQS 서버에게 메세지 발행
    }


    private List<TrendingPostProjection> getTrendingPosts(int n) {
        return postRepository.findTrendingPosts(n);
    }

}
