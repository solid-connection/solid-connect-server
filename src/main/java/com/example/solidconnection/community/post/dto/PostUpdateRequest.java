package com.example.solidconnection.community.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @NotNull(message = "게시글 카테고리를 설정해주세요.")
        String postCategory,

        @NotBlank(message = "게시글 제목은 빈 값일 수 없습니다.")
        @Size(min = 1, max = 255, message = "댓글 내용은 최소 1자 이상, 최대 255자 이하여야 합니다.")
        String title,

        @NotBlank(message = "게시글 내용은 빈 값일 수 없습니다.")
        @Size(min = 1, max = 1000, message = "댓글 내용은 최소 1자 이상, 최대 255자 이하여야 합니다.")
        String content
) {
}
