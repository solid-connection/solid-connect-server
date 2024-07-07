package com.example.solidconnection.siteuser.service;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.LikedUniversity;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.siteuser.dto.MyPageUpdateRequest;
import com.example.solidconnection.siteuser.dto.MyPageUpdateResponse;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.dto.UniversityPreviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.example.solidconnection.constants.Constants.MIN_DAYS_BETWEEN_NICKNAME_CHANGES;
import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_CHANGE_NICKNAME_YET;
import static com.example.solidconnection.custom.exception.ErrorCode.NICKNAME_ALREADY_EXISTED;

@RequiredArgsConstructor
@Service
public class MyPageService {

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
        return MyPageUpdateResponse.of(siteUser);
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
        return MyPageUpdateResponse.of(siteUser);
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
