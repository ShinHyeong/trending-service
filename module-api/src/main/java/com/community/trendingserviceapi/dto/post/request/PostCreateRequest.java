package com.community.trendingserviceapi.dto.post.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 50, message = "제목은 50자 이하여야 합니다.")
        String title,

        String content
) {
}
