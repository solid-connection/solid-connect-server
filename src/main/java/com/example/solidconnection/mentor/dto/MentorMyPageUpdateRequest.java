package com.example.solidconnection.mentor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record MentorMyPageUpdateRequest(
        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickname,

        @NotBlank(message = "자기소개를 입력해주세요.")
        String introduction,

        @NotBlank(message = "합격 레시피를 입력해주세요.")
        String passTip,

        @Valid
        List<ChannelRequest> channels
) {
}
