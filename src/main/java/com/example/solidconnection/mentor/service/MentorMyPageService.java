package com.example.solidconnection.mentor.service;

import static com.example.solidconnection.common.exception.ErrorCode.CHANNEL_REGISTRATION_LIMIT_EXCEEDED;
import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Channel;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.dto.ChannelRequest;
import com.example.solidconnection.mentor.dto.MentorMyPageResponse;
import com.example.solidconnection.mentor.dto.MentorMyPageUpdateRequest;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MentorMyPageService {

    private static final int CHANNEL_REGISTRATION_LIMIT = 4;
    private static final int CHANNEL_SEQUENCE_START_NUMBER = 1;

    private final MentorRepository mentorRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional(readOnly = true)
    public MentorMyPageResponse getMentorMyPage(long siteUserId) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Mentor mentor = mentorRepository.findBySiteUserId(siteUser.getId())
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
        return MentorMyPageResponse.of(mentor, siteUser);
    }

    @Transactional
    public void updateMentorMyPage(long siteUserId, MentorMyPageUpdateRequest request) {
        validateChannelRegistrationLimit(request.channels());
        Mentor mentor = mentorRepository.findBySiteUserId(siteUserId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));

        mentor.updateIntroduction(request.introduction());
        mentor.updatePassTip(request.passTip());
        updateChannel(request.channels(), mentor);
    }

    private void validateChannelRegistrationLimit(List<ChannelRequest> channelRequests) {
        if (channelRequests.size() > CHANNEL_REGISTRATION_LIMIT) {
            throw new CustomException(CHANNEL_REGISTRATION_LIMIT_EXCEEDED);
        }
    }

    private void updateChannel(List<ChannelRequest> channelRequests, Mentor mentor) {
        int sequence = CHANNEL_SEQUENCE_START_NUMBER;
        List<Channel> newChannels = new ArrayList<>();
        for (ChannelRequest request : channelRequests) {
            newChannels.add(new Channel(sequence++, request.type(), request.url()));
        }
        mentor.updateChannels(newChannels);
    }
}
