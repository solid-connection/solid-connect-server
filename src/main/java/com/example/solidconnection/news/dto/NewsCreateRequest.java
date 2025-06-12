package com.example.solidconnection.news.dto;

import com.example.solidconnection.news.domain.News;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NewsCreateRequest(
        @NotNull(message = "소식지 제목을 입력해주세요.")
        @Size(min = 1, max = 255, message = "소식지 제목은 최소 1자 이상, 최대 255자 이하여야 합니다.")
        String title,

        @NotNull(message = "소식지 설명을 입력해주세요.")
        @Size(min = 1, max = 255, message = "소식지 설명은 최소 1자 이상, 최대 255자 이하여야 합니다.")
        String description,

        @NotNull
        @Size(min = 1, max = 500, message = "소식지 URL은 최소 1자 이상, 최대 500자 이하여야 합니다.")
        String url
) {
    public News toEntity(String thumbnailUrl) {
        return new News(
                title,
                description,
                thumbnailUrl,
                url
        );
    }
}
