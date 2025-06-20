package com.example.solidconnection.news.dto;

import jakarta.validation.constraints.Size;

public record NewsUpdateRequest(

        @Size(max = 255, message = "소식지 제목은 최대 255자 이하여야 합니다.")
        String title,

        @Size(max = 255, message = "소식지 설명은 최대 255자 이하여야 합니다.")
        String description,

        @Size(max = 500, message = "소식지 URL은 최대 500자 이하여야 합니다.")
        String url
) {
}
