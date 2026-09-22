package com.community.trendingserviceapi.domain;

import com.community.trendingserviceapi.domain.post.Post;
import com.community.trendingserviceapi.domain.user.Account;

public record PostWithAccount(
        Post post,
        Account account
) {
    private static final String WITHDRAWN_NICKNAME = "탈퇴한 사용자";

    // 작성자가 탈퇴한 경우
    public String authorNickname() {
        return account != null ? account.getNickname() : WITHDRAWN_NICKNAME;
    }
}
