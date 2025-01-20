package com.example.solidconnection.siteuser.service;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.s3.S3Service;
import com.example.solidconnection.s3.UploadedFileUrlResponse;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.siteuser.dto.MyPageUpdateResponse;
import com.example.solidconnection.siteuser.dto.NicknameUpdateRequest;
import com.example.solidconnection.siteuser.dto.NicknameUpdateResponse;
import com.example.solidconnection.siteuser.dto.ProfileImageUpdateResponse;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.ImgType;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.example.solidconnection.university.domain.LikedUniversity;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_CHANGE_NICKNAME_YET;
import static com.example.solidconnection.custom.exception.ErrorCode.NICKNAME_ALREADY_EXISTED;
import static com.example.solidconnection.custom.exception.ErrorCode.PROFILE_IMAGE_NEEDED;
import static com.example.solidconnection.siteuser.service.SiteUserService.MIN_DAYS_BETWEEN_NICKNAME_CHANGES;
import static com.example.solidconnection.siteuser.service.SiteUserService.NICKNAME_LAST_CHANGE_DATE_FORMAT;
import static com.example.solidconnection.support.integration.TestDataSetUpHelper.괌대학_A_지원_정보;
import static com.example.solidconnection.support.integration.TestDataSetUpHelper.메이지대학_지원_정보;
import static com.example.solidconnection.support.integration.TestDataSetUpHelper.코펜하겐IT대학_지원_정보;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;

@DisplayName("유저 서비스 테스트")
class SiteUserServiceTest extends BaseIntegrationTest {

    @Autowired
    private SiteUserService siteUserService;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private LikedUniversityRepository likedUniversityRepository;

    @Test
    void 마이페이지_정보를_조회한다() {
        // given
        SiteUser testUser = createSiteUser();
        int likedUniversityCount = createLikedUniversities(testUser);

        // when
        MyPageResponse response = siteUserService.getMyPageInfo(testUser.getEmail());

        // then
        Assertions.assertAll(
                () -> assertThat(response.nickname()).isEqualTo(testUser.getNickname()),
                () -> assertThat(response.profileImageUrl()).isEqualTo(testUser.getProfileImageUrl()),
                () -> assertThat(response.role()).isEqualTo(testUser.getRole()),
                () -> assertThat(response.birth()).isEqualTo(testUser.getBirth()),
                () -> assertThat(response.email()).isEqualTo(testUser.getEmail()),
                () -> assertThat(response.likedPostCount()).isEqualTo(testUser.getPostLikeList().size()),
                () -> assertThat(response.likedUniversityCount()).isEqualTo(likedUniversityCount)
        );
    }

    @Test
    void 내_정보를_수정하기_위한_마이페이지_정보를_조회한다() {
        // given
        SiteUser testUser = createSiteUser();

        // when
        MyPageUpdateResponse response = siteUserService.getMyPageInfoToUpdate(testUser.getEmail());

        // then
        Assertions.assertAll(
                () -> assertThat(response.nickname()).isEqualTo(testUser.getNickname()),
                () -> assertThat(response.profileImageUrl()).isEqualTo(testUser.getProfileImageUrl())
        );
    }

    @Test
    void 관심_대학교_목록을_조회한다() {
        // given
        SiteUser testUser = createSiteUser();
        int likedUniversityCount = createLikedUniversities(testUser);

        // when
        List<UniversityInfoForApplyPreviewResponse> response = siteUserService.getWishUniversity(testUser.getEmail());

        // then
        assertThat(response)
                .hasSize(likedUniversityCount)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsAll(List.of(
                        UniversityInfoForApplyPreviewResponse.from(괌대학_A_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(메이지대학_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(코펜하겐IT대학_지원_정보)
                ));
    }

    @Test
    void 새로운_이미지로_성공적으로_업데이트한다() {
        // given
        SiteUser testUser = createSiteUser();
        String expectedUrl = "newProfileImageUrl";
        MockMultipartFile imageFile = createValidImageFile();
        given(s3Service.uploadFile(any(), eq(ImgType.PROFILE)))
                .willReturn(new UploadedFileUrlResponse(expectedUrl));

        // when
        ProfileImageUpdateResponse response = siteUserService.updateProfileImage(
                testUser.getEmail(),
                imageFile
        );

        // then
        assertThat(response.profileImageUrl()).isEqualTo(expectedUrl);
    }

    @Test
    void 프로필을_처음_수정하는_것이면_이전_이미지를_삭제하지_않는다() {
        // given
        SiteUser testUser = createSiteUser();
        MockMultipartFile imageFile = createValidImageFile();
        given(s3Service.uploadFile(any(), eq(ImgType.PROFILE)))
                .willReturn(new UploadedFileUrlResponse("newProfileImageUrl"));

        // when
        siteUserService.updateProfileImage(testUser.getEmail(), imageFile);

        // then
        then(s3Service).should(never()).deleteExProfile(any());
    }

    @Test
    void 프로필을_처음_수정하는_것이_아니라면_이전_이미지를_삭제한다() {
        // given
        SiteUser testUser = createSiteUserWithCustomProfile();
        MockMultipartFile imageFile = createValidImageFile();
        given(s3Service.uploadFile(any(), eq(ImgType.PROFILE)))
                .willReturn(new UploadedFileUrlResponse("newProfileImageUrl"));

        // when
        siteUserService.updateProfileImage(testUser.getEmail(), imageFile);

        // then
        then(s3Service).should().deleteExProfile(testUser.getEmail());
    }

    @Test
    void 빈_이미지_파일로_프로필을_수정하면_예외를_반환한다() {
        // given
        SiteUser testUser = createSiteUser();
        MockMultipartFile emptyFile = createEmptyImageFile();

        // when & then
        assertThatCode(() -> siteUserService.updateProfileImage(testUser.getEmail(), emptyFile))
                .isInstanceOf(CustomException.class)
                .hasMessage(PROFILE_IMAGE_NEEDED.getMessage());
    }

    @Test
    void 닉네임을_성공적으로_수정한다() {
        // given
        SiteUser testUser = createSiteUser();
        String newNickname = "newNickname";
        NicknameUpdateRequest request = new NicknameUpdateRequest(newNickname);

        // when
        NicknameUpdateResponse response = siteUserService.updateNickname(
                testUser.getEmail(),
                request
        );

        // then
        SiteUser updatedUser = siteUserRepository.getByEmail(testUser.getEmail());
        assertThat(updatedUser.getNicknameModifiedAt()).isNotNull();
        assertThat(response.nickname()).isEqualTo(newNickname);
    }

    @Test
    void 중복된_닉네임으로_변경시_예외가_발생한다() {
        // given
        createDuplicatedSiteUser();
        SiteUser testUser = createSiteUser();
        NicknameUpdateRequest request = new NicknameUpdateRequest("duplicatedNickname");

        // when & then
        assertThatCode(() -> siteUserService.updateNickname(testUser.getEmail(), request))
                .isInstanceOf(CustomException.class)
                .hasMessage(NICKNAME_ALREADY_EXISTED.getMessage());
    }

    @Test
    void 최소_대기기간이_지나지_않은_상태에서_변경시_예외가_발생한다() {
        // given
        SiteUser testUser = createSiteUser();
        LocalDateTime modifiedAt = LocalDateTime.now().minusDays(MIN_DAYS_BETWEEN_NICKNAME_CHANGES - 1);
        testUser.setNicknameModifiedAt(modifiedAt);
        siteUserRepository.save(testUser);

        NicknameUpdateRequest request = new NicknameUpdateRequest("newNickname");

        // when & then
        assertThatCode(() ->
                siteUserService.updateNickname(testUser.getEmail(), request))
                .isInstanceOf(CustomException.class)
                .hasMessage(createExpectedErrorMessage(modifiedAt));
    }

    private SiteUser createSiteUser() {
        SiteUser siteUser = new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
        return siteUserRepository.save(siteUser);
    }

    private SiteUser createSiteUserWithCustomProfile() {
        SiteUser siteUser = new SiteUser(
                "test@example.com",
                "nickname",
                "profile/profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
        return siteUserRepository.save(siteUser);
    }

    private void createDuplicatedSiteUser() {
        SiteUser siteUser = new SiteUser(
                "duplicated@example.com",
                "duplicatedNickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
        siteUserRepository.save(siteUser);
    }

    private int createLikedUniversities(SiteUser testUser) {
        LikedUniversity likedUniversity1 = new LikedUniversity(null, 괌대학_A_지원_정보, testUser);
        LikedUniversity likedUniversity2 = new LikedUniversity(null, 메이지대학_지원_정보, testUser);
        LikedUniversity likedUniversity3 = new LikedUniversity(null, 코펜하겐IT대학_지원_정보, testUser);

        likedUniversityRepository.save(likedUniversity1);
        likedUniversityRepository.save(likedUniversity2);
        likedUniversityRepository.save(likedUniversity3);
        return likedUniversityRepository.countBySiteUser_Email(testUser.getEmail());
    }

    private MockMultipartFile createValidImageFile() {
        return new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    private MockMultipartFile createEmptyImageFile() {
        return new MockMultipartFile(
                "image",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );
    }

    private String createExpectedErrorMessage(LocalDateTime modifiedAt) {
        String formatLastModifiedAt = String.format(
                "(마지막 수정 시간 : %s)",
                NICKNAME_LAST_CHANGE_DATE_FORMAT.format(modifiedAt)
        );
        return CAN_NOT_CHANGE_NICKNAME_YET.getMessage() + " : " + formatLastModifiedAt;
    }
}
