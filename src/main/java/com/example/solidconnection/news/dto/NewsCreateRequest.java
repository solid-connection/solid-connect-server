package com.example.solidconnection.news.dto;

import com.example.solidconnection.news.domain.News;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record NewsCreateRequest(
        @NotBlank(message = "소식지 제목을 입력해주세요.")
        @Size(max = 20, message = "소식지 제목은 20자 이하여야 합니다.")
        String title,

        @NotBlank(message = "소식지 내용을 입력해주세요.")
        @Size(max = 30, message = "소식지 내용은 30자 이하여야 합니다.")
        String description,

        @NotBlank(message = "소식지 URL을 입력해주세요.")
        @Size(max = 500, message = "소식지 URL은 500자 이하여야 합니다.")
        @URL(message = "올바른 URL 형식이 아닙니다.")
        String url
) {
    public News toEntity(String thumbnailUrl, long siteUserId) {
        return new News(
                title,
                description,
                thumbnailUrl,
                url,
                siteUserId
        );
    }
}
