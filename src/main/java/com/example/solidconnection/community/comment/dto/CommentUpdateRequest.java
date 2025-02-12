package com.example.solidconnection.community.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        @NotBlank(message = "댓글 내용은 빈 값일 수 없습니다.")
        @Size(min = 1, max = 255, message = "댓글 내용은 최소 1자 이상, 최대 255자 이하여야 합니다.")
        String content
) {
}
