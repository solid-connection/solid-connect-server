package com.example.solidconnection.mentor.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Channel;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.dto.ChannelRequest;
import com.example.solidconnection.mentor.dto.MentorMyPageUpdateRequest;
import com.example.solidconnection.mentor.dto.MentorMyPageResponse;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class MentorMyPageService {

    private static final int CHANNEL_SEQUENCE_START_NUMBER = 1;

    private final MyPageService myPageService;
    private final MentorRepository mentorRepository;

    @Transactional(readOnly = true)
    public MentorMyPageResponse getMentorMyPage(SiteUser siteUser) {
        Mentor mentor = mentorRepository.findBySiteUserId(siteUser.getId())
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
        return MentorMyPageResponse.of(mentor, siteUser);
    }

    @Transactional
    public void updateMentorMyPage(SiteUser siteUser, MentorMyPageUpdateRequest mentorMyPageUpdateRequest, MultipartFile imageFile) {
        Mentor mentor = mentorRepository.findBySiteUserId(siteUser.getId())
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));

        myPageService.updateMyPageInfo(siteUser, imageFile, mentorMyPageUpdateRequest.nickname());
        mentor.updateIntroduction(mentorMyPageUpdateRequest.introduction());
        mentor.updatePassTip(mentorMyPageUpdateRequest.passTip());

        int sequence = CHANNEL_SEQUENCE_START_NUMBER;
        List<Channel> newChannels = new ArrayList<>();
        for (ChannelRequest request : mentorMyPageUpdateRequest.channels()) {
            newChannels.add(new Channel(sequence++, request.type(), request.url()));
        }
        mentor.updateChannels(newChannels);
    }
}
