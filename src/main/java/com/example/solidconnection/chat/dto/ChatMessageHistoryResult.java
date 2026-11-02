package com.example.solidconnection.chat.dto;

import com.example.solidconnection.common.dto.SliceResponse;
import java.util.List;

public record ChatMessageHistoryResult(
        SliceResponse<ChatMessageResponse> response,
        long participantId,
        List<Long> messageIds
) {
}
