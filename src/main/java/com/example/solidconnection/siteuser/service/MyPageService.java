package com.example.solidconnection.siteuser.service;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.siteuser.dto.MyPageUpdateRequest;
import com.example.solidconnection.siteuser.dto.MyPageUpdateResponse;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.domain.LikedUniversity;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_CHANGE_NICKNAME_YET;
import static com.example.solidconnection.custom.exception.ErrorCode.NICKNAME_ALREADY_EXISTED;

@RequiredArgsConstructor
@Service
public class MyPageService {

    public static final int MIN_DAYS_BETWEEN_NICKNAME_CHANGES = 30;
    public static final DateTimeFormatter NICKNAME_LAST_CHANGE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final SiteUserRepository siteUserRepository;
    private final LikedUniversityRepository likedUniversityRepository;

    /*
     * 마이페이지 정보를 조회한다.
     * */
    @Transactional(readOnly = true)
    public MyPageResponse getMyPageInfo(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        int likedUniversityCount = likedUniversityRepository.countBySiteUser_Email(email);
        return MyPageResponse.of(siteUser, likedUniversityCount);
    }

    /*
     * 내 정보를 수정하기 위한 마이페이지 정보를 조회한다. (닉네임, 프로필 사진)
     * */
    @Transactional(readOnly = true)
    public MyPageUpdateResponse getMyPageInfoToUpdate(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        return MyPageUpdateResponse.from(siteUser);
    }

    /*
     * 마이페이지 정보를 수정한다.
     * - 정보를 수정하기 전에 닉네임 중복, 닉네임 변경 최소 기간을 검증한다.
     * */
    @Transactional
    public MyPageUpdateResponse update(String email, MyPageUpdateRequest pageUpdateRequest) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);

        validateNicknameDuplicated(pageUpdateRequest.nickname());
        validateNicknameNotChangedRecently(siteUser.getNicknameModifiedAt());

        siteUser.setNickname(pageUpdateRequest.nickname());
        siteUser.setProfileImageUrl(pageUpdateRequest.profileImageUrl());
        siteUser.setNicknameModifiedAt(LocalDateTime.now());
        siteUserRepository.save(siteUser);
        return MyPageUpdateResponse.from(siteUser);
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
            String formatLastModifiedAt
                    = String.format("(마지막 수정 시간 : %s) ", NICKNAME_LAST_CHANGE_DATE_FORMAT.format(lastModifiedAt));
            throw new CustomException(CAN_NOT_CHANGE_NICKNAME_YET, formatLastModifiedAt);
        }
    }

    /*
     * 관심 대학교 목록을 조회한다.
     * */
    @Transactional(readOnly = true)
    public List<UniversityInfoForApplyPreviewResponse> getWishUniversity(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        List<LikedUniversity> likedUniversities = likedUniversityRepository.findAllBySiteUser_Email(siteUser.getEmail());
        return likedUniversities.stream()
                .map(likedUniversity -> UniversityInfoForApplyPreviewResponse.from(likedUniversity.getUniversityInfoForApply()))
                .toList();
    }
}
