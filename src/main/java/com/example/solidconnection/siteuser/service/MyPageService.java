package com.example.solidconnection.siteuser.service;

import static com.example.solidconnection.common.exception.ErrorCode.CAN_NOT_CHANGE_NICKNAME_YET;
import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.NICKNAME_ALREADY_EXISTED;
import static com.example.solidconnection.common.exception.ErrorCode.UNIVERSITY_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.location.country.repository.CountryRepository;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.repository.LikedUnivApplyInfoRepository;
import com.example.solidconnection.university.repository.UniversityRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class MyPageService {

    public static final int MIN_DAYS_BETWEEN_NICKNAME_CHANGES = 7;
    public static final DateTimeFormatter NICKNAME_LAST_CHANGE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final SiteUserRepository siteUserRepository;
    private final LikedUnivApplyInfoRepository likedUnivApplyInfoRepository;
    private final CountryRepository countryRepository;
    private final MentorRepository mentorRepository;
    private final UniversityRepository universityRepository;
    private final S3Service s3Service;

    /*
     * 마이페이지 정보를 조회한다.
     * */
    @Transactional(readOnly = true)
    public MyPageResponse getMyPageInfo(long siteUserId) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        int likedUnivApplyInfoCount = likedUnivApplyInfoRepository.countBySiteUserId(siteUser.getId());

        List<String> interestedCountries = null;
        String universityKoreanName = null;
        if (siteUser.getRole() == Role.MENTEE) {
            interestedCountries = countryRepository.findKoreanNamesBySiteUserId(siteUser.getId());
        } else if (siteUser.getRole() == Role.MENTOR) {
            Mentor mentor = mentorRepository.findBySiteUserId(siteUser.getId())
                    .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
            University university = universityRepository.findById(mentor.getUniversityId())
                    .orElseThrow(() -> new CustomException(UNIVERSITY_NOT_FOUND));
            universityKoreanName = university.getKoreanName();
        }
        return MyPageResponse.of(siteUser, likedUnivApplyInfoCount, interestedCountries, universityKoreanName);
    }

    /*
     * 마이페이지 정보를 수정한다.
     * */
    @Transactional
    public void updateMyPageInfo(long siteUserId, MultipartFile imageFile, String nickname) {
        SiteUser user = siteUserRepository.findById(siteUserId)
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
                s3Service.deleteExProfile(user.getId());
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
}
