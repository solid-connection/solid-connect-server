package com.example.solidconnection.siteuser.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.university.repository.LikedUnivApplyInfoRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.domain.LikedUnivApplyInfo;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.example.solidconnection.common.exception.ErrorCode.CAN_NOT_CHANGE_NICKNAME_YET;
import static com.example.solidconnection.common.exception.ErrorCode.NICKNAME_ALREADY_EXISTED;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class MyPageService {

    public static final int MIN_DAYS_BETWEEN_NICKNAME_CHANGES = 7;
    public static final DateTimeFormatter NICKNAME_LAST_CHANGE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final SiteUserRepository siteUserRepository;
    private final LikedUnivApplyInfoRepository likedUnivApplyInfoRepository;
    private final S3Service s3Service;

    /*
     * 마이페이지 정보를 조회한다.
     * */
    @Transactional(readOnly = true)
    public MyPageResponse getMyPageInfo(SiteUser siteUser) {
        int likedUnivApplyInfoCount = likedUnivApplyInfoRepository.countBySiteUser_Id(siteUser.getId());
        return MyPageResponse.of(siteUser, likedUnivApplyInfoCount);
    }

    /*
     * 마이페이지 정보를 수정한다.
     * */
    @Transactional
    public void updateMyPageInfo(SiteUser siteUser, MultipartFile imageFile, String nickname) {
        SiteUser user = siteUserRepository.findById(siteUser.getId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (nickname != null) {
            validateNicknameNotChangedRecently(user.getNicknameModifiedAt());
            validateNicknameUnique(nickname);
            user.setNickname(nickname);
            user.setNicknameModifiedAt(LocalDateTime.now());
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(imageFile, ImgType.PROFILE);
            if (!isDefaultProfileImage(user.getProfileImageUrl())) {
                s3Service.deleteExProfile(user);
            }
            String profileImageUrl = uploadedFile.fileUrl();
            user.setProfileImageUrl(profileImageUrl);
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

    private void validateNicknameUnique(String nickname) {
        if (siteUserRepository.existsByNickname(nickname)) {
            throw new CustomException(NICKNAME_ALREADY_EXISTED);
        }
    }

    private boolean isDefaultProfileImage(String profileImageUrl) {
        String prefix = "profile/";
        return profileImageUrl == null || !profileImageUrl.startsWith(prefix);
    }

    /*
     * 관심 대학교 목록을 조회한다.
     * */
    @Transactional(readOnly = true)
    public List<UnivApplyInfoPreviewResponse> getWishUnivApplyInfo(SiteUser siteUser) {
        List<LikedUnivApplyInfo> likedUnivApplyInfos = likedUnivApplyInfoRepository.findAllBySiteUser_Id(siteUser.getId());
        return likedUnivApplyInfos.stream()
                .map(likedUnivApplyInfo -> UnivApplyInfoPreviewResponse.from(likedUnivApplyInfo.getUnivApplyInfo()))
                .toList();
    }
}
