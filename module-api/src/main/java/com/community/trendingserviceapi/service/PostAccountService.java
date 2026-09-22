package com.community.trendingserviceapi.service;

import com.community.trendingserviceapi.domain.PostWithAccount;
import com.community.trendingserviceapi.domain.post.Post;
import com.community.trendingserviceapi.domain.post.PostRepository;
import com.community.trendingserviceapi.domain.user.Account;
import com.community.trendingserviceapi.domain.user.AccountRepository;
import com.community.trendingserviceapi.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostAccountService {
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    public List<PostWithAccount> getPostWithAccounts(List<Long> postIds) {
        Map<Long, Post> postsByPostId = postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getPostId, Function.identity()));

        List<Long> userIds = postsByPostId.values().stream()
                .map(Post::getUserId)
                .distinct()
                .toList();

        Map<Long, Account> accountsByUserId = accountRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(Account::getUserId, Function.identity()));

        return postIds.stream()
                .map(postsByPostId::get)
                .filter(Objects::nonNull)
                .map(post -> new PostWithAccount(post, accountsByUserId.get(post.getUserId())))
                .toList();
    }

    public PostWithAccount getPostWithAccount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        Account account = accountRepository.findById(post.getUserId())
                .orElse(null);
        return new PostWithAccount(post, account);
    }
}
