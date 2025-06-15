package com.example.solidconnection.siteuser.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.domain.LikedUniversity;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.example.solidconnection.common.exception.ErrorCode.CAN_NOT_CHANGE_NICKNAME_YET;

@RequiredArgsConstructor
@Service
public class MyPageService {

    public static final int MIN_DAYS_BETWEEN_NICKNAME_CHANGES = 7;
    public static final DateTimeFormatter NICKNAME_LAST_CHANGE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final SiteUserRepository siteUserRepository;
    private final LikedUniversityRepository likedUniversityRepository;
    private final S3Service s3Service;

    /*
     * 마이페이지 정보를 조회한다.
     * */
    @Transactional(readOnly = true)
    public MyPageResponse getMyPageInfo(SiteUser siteUser) {
        int likedUniversityCount = likedUniversityRepository.countBySiteUser_Id(siteUser.getId());
        return MyPageResponse.of(siteUser, likedUniversityCount);
    }

    /*
     * 마이페이지 정보를 수정한다.
     * */
    @Transactional
    public void updateMyPageInfo(SiteUser siteUser, MultipartFile imageFile, String nickname) {
        if (nickname != null) {
            validateNicknameNotChangedRecently(siteUser.getNicknameModifiedAt());

            try {
                siteUserRepository.updateNickname(siteUser.getId(), nickname, LocalDateTime.now());
            } catch (DataIntegrityViolationException e) {
                throw new CustomException(determineErrorCode(e));
            }
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(imageFile, ImgType.PROFILE);
            if (!isDefaultProfileImage(siteUser.getProfileImageUrl())) {
                s3Service.deleteExProfile(siteUser);
            }
            String profileImageUrl = uploadedFile.fileUrl();
            siteUserRepository.updateProfileImage(siteUser.getId(), profileImageUrl);
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

    private boolean isDefaultProfileImage(String profileImageUrl) {
        String prefix = "profile/";
        return profileImageUrl == null || !profileImageUrl.startsWith(prefix);
    }

    private ErrorCode determineErrorCode(DataIntegrityViolationException e) {
        if (e.getMessage().contains("uk_site_user_nickname")) {
            return ErrorCode.NICKNAME_ALREADY_EXISTED;
        }

        return ErrorCode.DATA_INTEGRITY_VIOLATION;
    }

    /*
     * 관심 대학교 목록을 조회한다.
     * */
    @Transactional(readOnly = true)
    public List<UniversityInfoForApplyPreviewResponse> getWishUniversity(SiteUser siteUser) {
        List<LikedUniversity> likedUniversities = likedUniversityRepository.findAllBySiteUser_Id(siteUser.getId());
        return likedUniversities.stream()
                .map(likedUniversity -> UniversityInfoForApplyPreviewResponse.from(likedUniversity.getUniversityInfoForApply()))
                .toList();
    }
}
