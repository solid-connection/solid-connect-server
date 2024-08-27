package com.example.solidconnection.siteuser.service;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.s3.S3Service;
import com.example.solidconnection.s3.UploadedFileUrlResponse;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.*;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.ImgType;
import com.example.solidconnection.university.domain.LikedUniversity;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.*;

@RequiredArgsConstructor
@Service
public class SiteUserService {

    public static final int MIN_DAYS_BETWEEN_NICKNAME_CHANGES = 7;
    public static final DateTimeFormatter NICKNAME_LAST_CHANGE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final SiteUserRepository siteUserRepository;
    private final LikedUniversityRepository likedUniversityRepository;
    private final S3Service s3Service;

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
                    = String.format("(마지막 수정 시간 : %s)", NICKNAME_LAST_CHANGE_DATE_FORMAT.format(lastModifiedAt));
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


    /*
     * 프로필 이미지를 수정한다.
     * */
    @Transactional
    public ProfileImageUpdateResponse updateProfileImage(String email, MultipartFile imageFile) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        validateProfileImage(imageFile);

        // 프로필 이미지를 처음 수정하는 경우에는 deleteExProfile 수행하지 않음
        if(!isDefaultProfileImage(siteUser.getProfileImageUrl())){
            s3Service.deleteExProfile(email);
        }
        UploadedFileUrlResponse uploadedFileUrlResponse = s3Service.uploadFile(imageFile, ImgType.PROFILE);
        siteUser.setProfileImageUrl(uploadedFileUrlResponse.fileUrl());
        siteUserRepository.save(siteUser);

        return ProfileImageUpdateResponse.from(siteUser);
    }

    private void validateProfileImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new CustomException(PROFILE_IMAGE_NEEDED);
        }
    }

    private boolean isDefaultProfileImage(String profileImageUrl) {
        String prefix = "https://solid-connection-uploaded.s3.ap-northeast-2.amazonaws.com/profile/";
        return profileImageUrl == null || !profileImageUrl.startsWith(prefix);
    }

    /*
     * 닉네임을 수정한다.
     * */
    @Transactional
    public NicknameUpdateResponse updateNickname(String email, NicknameUpdateRequest nicknameUpdateRequest) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);

        validateNicknameDuplicated(nicknameUpdateRequest.nickname());
        validateNicknameNotChangedRecently(siteUser.getNicknameModifiedAt());

        siteUser.setNickname(nicknameUpdateRequest.nickname());
        siteUser.setNicknameModifiedAt(LocalDateTime.now());
        siteUserRepository.save(siteUser);

        return NicknameUpdateResponse.from(siteUser);
    }
}
