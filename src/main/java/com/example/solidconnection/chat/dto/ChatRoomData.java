package com.example.solidconnection.chat.dto;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.siteuser.domain.SiteUser;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ChatRoomData(
        Map<Long, ChatMessage> latestMessages,
        Map<Long, Long> unreadCounts,
        Map<Long, SiteUser> partnerUsers
) {

    public static ChatRoomData from(List<ChatMessage> latestMessages,
                                    List<UnreadCountDto> unreadCounts,
                                    List<SiteUser> partnerUsers) {
        return new ChatRoomData(
                latestMessages.stream().collect(Collectors.toMap(msg -> msg.getChatRoom().getId(), msg -> msg)),
                unreadCounts.stream().collect(Collectors.toMap(UnreadCountDto::chatRoomId, UnreadCountDto::count)),
                partnerUsers.stream().collect(Collectors.toMap(SiteUser::getId, user -> user))
        );
    }
}
