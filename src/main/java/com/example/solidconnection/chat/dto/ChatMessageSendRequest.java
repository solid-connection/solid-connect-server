package com.example.solidconnection.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatMessageSendRequest(
        @NotNull(message = "메시지를 입력해주세요.")
        @Size(max = 500, message = "메시지는 500자를 초과할 수 없습니다")
        String content
) {

}
