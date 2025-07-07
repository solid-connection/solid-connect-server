package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record ChannelRequest(
        @NotNull(message = "채널 종류를 입력해주세요.")
        ChannelType type,

        @NotBlank(message = "채널 URL을 입력해주세요.")
        @URL
        String url
) {
}
