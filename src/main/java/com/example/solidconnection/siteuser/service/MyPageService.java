package com.example.solidconnection.siteuser.service;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.LikedUniversity;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.siteuser.dto.MyPageDto;
import com.example.solidconnection.siteuser.dto.MyPageUpdateDto;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.dto.UniversityPreviewDto;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.example.solidconnection.constants.Constants.MIN_DAYS_BETWEEN_NICKNAME_CHANGES;
import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_CHANGE_NICKNAME_YET;
import static com.example.solidconnection.custom.exception.ErrorCode.NICKNAME_ALREADY_EXISTED;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final SiteUserRepository siteUserRepository;
    private final LikedUniversityRepository likedUniversityRepository;
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;

    public MyPageDto getMyPageInfo(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        int likedUniversityCount = likedUniversityRepository.countBySiteUser_Email(email);
        return MyPageDto.fromEntity(siteUser, likedUniversityCount);
    }

    public MyPageUpdateDto getMyPageInfoToUpdate(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        return MyPageUpdateDto.fromEntity(siteUser);
    }

    @Transactional
    public void update(String email, MyPageUpdateDto myPageUpdateDto) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        validateNicknameDuplicated(myPageUpdateDto.getNickname());
        validateNicknameNotChangedRecently(siteUser.getNicknameModifiedAt());
        siteUser.setNickname(myPageUpdateDto.getNickname());
        siteUser.setProfileImageUrl(myPageUpdateDto.getProfileImageUrl());
        siteUser.setNicknameModifiedAt(LocalDateTime.now());
    }

    private void validateNicknameDuplicated(String nickname) {
        if (siteUserRepository.existsByNickname(nickname)) {
            throw new CustomException(NICKNAME_ALREADY_EXISTED);
        }
    }

    private void validateNicknameNotChangedRecently(LocalDateTime lastModifiedAt) {
        if (lastModifiedAt == null) {
            return;
        }
        if (LocalDateTime.now().isBefore(lastModifiedAt.plusDays(MIN_DAYS_BETWEEN_NICKNAME_CHANGES))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formatLastModifiedAt = String.format("(마지막 수정 시간 : %s) ", formatter.format(lastModifiedAt));
            throw new CustomException(CAN_NOT_CHANGE_NICKNAME_YET, formatLastModifiedAt);
        }
    }

    public List<UniversityPreviewDto> getWishUniversity(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        List<LikedUniversity> likedUniversities = likedUniversityRepository.findAllBySiteUser_Email(siteUser.getEmail());
        return likedUniversities.stream()
                .map(likedUniversity -> UniversityPreviewDto.fromEntity(likedUniversity.getUniversityInfoForApply()))
                .toList();
    }
}
