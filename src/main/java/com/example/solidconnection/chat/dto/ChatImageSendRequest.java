package com.example.solidconnection.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ChatImageSendRequest(
        @NotNull(message = "이미지 URL 목록은 필수입니다")
        @Size(min = 1, max = 10, message = "이미지는 1~10개까지 가능합니다")
        List<@NotBlank(message = "이미지 URL은 필수입니다") String> imageUrls
) {

}
