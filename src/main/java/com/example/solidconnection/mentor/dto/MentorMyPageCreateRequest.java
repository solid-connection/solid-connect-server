package com.example.solidconnection.mentor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record MentorMyPageCreateRequest(
        @NotBlank(message = "자기소개를 입력해주세요.")
        String introduction,

        @NotBlank(message = "합격 레시피를 입력해주세요.")
        String passTip,

        @NotNull
        @Valid
        List<ChannelRequest> channels
) {

}
