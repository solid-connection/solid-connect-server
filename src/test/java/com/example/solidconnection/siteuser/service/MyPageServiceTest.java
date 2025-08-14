package com.example.solidconnection.siteuser.service;

import static com.example.solidconnection.common.exception.ErrorCode.CAN_NOT_CHANGE_NICKNAME_YET;
import static com.example.solidconnection.siteuser.service.MyPageService.MIN_DAYS_BETWEEN_NICKNAME_CHANGES;
import static com.example.solidconnection.siteuser.service.MyPageService.NICKNAME_LAST_CHANGE_DATE_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.location.country.domain.Country;
import com.example.solidconnection.location.country.domain.InterestedCountry;
import com.example.solidconnection.location.country.fixture.CountryFixture;
import com.example.solidconnection.location.country.repository.InterestedCountryRepository;
import com.example.solidconnection.mentor.fixture.MentorFixture;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.siteuser.fixture.SiteUserFixtureBuilder;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.LikedUnivApplyInfo;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import com.example.solidconnection.university.repository.LikedUnivApplyInfoRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

@TestContainerSpringBootTest
@DisplayName("마이페이지 서비스 테스트")
class MyPageServiceTest {

    @Autowired
    private MyPageService myPageService;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private LikedUnivApplyInfoRepository likedUnivApplyInfoRepository;

    @Autowired
    private InterestedCountryRepository interestedCountryRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private MentorFixture mentorFixture;

    @Autowired
    private CountryFixture countryFixture;

    @Autowired
    private UnivApplyInfoFixture univApplyInfoFixture;

    @Autowired
    private SiteUserFixtureBuilder siteUserFixtureBuilder;

    private SiteUser user;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
    }

    @Test
    void 멘티의_마이페이지_정보를_조회한다() {
        // given
        int likedUnivApplyInfoCount = createLikedUnivApplyInfos(user);
        Country country = countryFixture.미국();
        InterestedCountry interestedCountry = new InterestedCountry(user, country);
        interestedCountryRepository.save(interestedCountry);

        // when
        MyPageResponse response = myPageService.getMyPageInfo(user.getId());

        // then
        Assertions.assertAll(
                () -> assertThat(response.nickname()).isEqualTo(user.getNickname()),
                () -> assertThat(response.profileImageUrl()).isEqualTo(user.getProfileImageUrl()),
                () -> assertThat(response.role()).isEqualTo(user.getRole()),
                () -> assertThat(response.email()).isEqualTo(user.getEmail()),
                // () -> assertThat(response.likedPostCount()).isEqualTo(user.getLikedPostList().size()),
                // todo : 좋아요한 게시물 수 반환 기능 추가와 함께 수정요망
                () -> assertThat(response.likedUnivApplyInfoCount()).isEqualTo(likedUnivApplyInfoCount),
                () -> assertThat(response.interestedCountries().get(0)).isEqualTo(country.getKoreanName()),
                () -> assertThat(response.attendedUniversity()).isNull()
        );
    }

    @Test
    void 멘토의_마이페이지_정보를_조회한다() {
        // given
        SiteUser mentorUser = siteUserFixture.멘토(1, "mentor");
        University university = univApplyInfoFixture.괌대학_A_지원_정보().getUniversity();
        mentorFixture.멘토(mentorUser.getId(), university.getId());
        int likedUnivApplyInfoCount = createLikedUnivApplyInfos(mentorUser);

        // when
        MyPageResponse response = myPageService.getMyPageInfo(mentorUser.getId());

        // then
        Assertions.assertAll(
                () -> assertThat(response.nickname()).isEqualTo(mentorUser.getNickname()),
                () -> assertThat(response.profileImageUrl()).isEqualTo(mentorUser.getProfileImageUrl()),
                () -> assertThat(response.role()).isEqualTo(mentorUser.getRole()),
                () -> assertThat(response.email()).isEqualTo(mentorUser.getEmail()),
                // () -> assertThat(response.likedPostCount()).isEqualTo(user.getLikedPostList().size()),
                // todo : 좋아요한 게시물 수 반환 기능 추가와 함께 수정요망
                () -> assertThat(response.likedUnivApplyInfoCount()).isEqualTo(likedUnivApplyInfoCount),
                () -> assertThat(response.attendedUniversity()).isEqualTo(university.getKoreanName()),
                () -> assertThat(response.interestedCountries()).isNull()
        );
    }

    @Nested
    class 프로필_이미지_수정_테스트 {

        @Test
        void 새로운_이미지로_성공적으로_업데이트한다() {
            // given
            String expectedUrl = "newProfileImageUrl";
            MockMultipartFile imageFile = createValidImageFile();
            given(s3Service.uploadFile(any(), eq(ImgType.PROFILE)))
                    .willReturn(new UploadedFileUrlResponse(expectedUrl));

            // when
            myPageService.updateMyPageInfo(user.getId(), imageFile, "newNickname");

            // then
            SiteUser updatedUser = siteUserRepository.findById(user.getId()).get();
            assertThat(updatedUser.getProfileImageUrl()).isEqualTo(expectedUrl);
        }

        @Test
        void 프로필을_처음_수정하는_것이면_이전_이미지를_삭제하지_않는다() {
            // given
            MockMultipartFile imageFile = createValidImageFile();
            given(s3Service.uploadFile(any(), eq(ImgType.PROFILE)))
                    .willReturn(new UploadedFileUrlResponse("newProfileImageUrl"));

            // when
            myPageService.updateMyPageInfo(user.getId(), imageFile, "newNickname");

            // then
            then(s3Service).should(never()).deleteExProfile(user.getId());
        }

        @Test
        void 프로필을_처음_수정하는_것이_아니라면_이전_이미지를_삭제한다() {
            // given
            SiteUser 커스텀_프로필_사용자 = createSiteUserWithCustomProfile();
            MockMultipartFile imageFile = createValidImageFile();
            given(s3Service.uploadFile(any(), eq(ImgType.PROFILE)))
                    .willReturn(new UploadedFileUrlResponse("newProfileImageUrl"));

            // when
            myPageService.updateMyPageInfo(커스텀_프로필_사용자.getId(), imageFile, "newNickname");

            // then
            then(s3Service).should().deleteExProfile(커스텀_프로필_사용자.getId());
        }
    }

    @Nested
    class 닉네임_수정_테스트 {

        @BeforeEach
        void setUp() {
            given(s3Service.uploadFile(any(), eq(ImgType.PROFILE)))
                    .willReturn(new UploadedFileUrlResponse("newProfileImageUrl"));
        }

        @Test
        void 닉네임을_성공적으로_수정한다() {
            // given
            MockMultipartFile imageFile = createValidImageFile();
            String newNickname = "newNickname";

            // when
            myPageService.updateMyPageInfo(user.getId(), imageFile, newNickname);

            // then
            SiteUser updatedUser = siteUserRepository.findById(user.getId()).get();
            assertThat(updatedUser.getNicknameModifiedAt()).isNotNull();
            assertThat(updatedUser.getNickname()).isEqualTo(newNickname);
        }

        @Test
        void 최소_대기기간이_지나지_않은_상태에서_변경하면_예외가_발생한다() {
            // given
            MockMultipartFile imageFile = createValidImageFile();
            LocalDateTime modifiedAt = LocalDateTime.now().minusDays(MIN_DAYS_BETWEEN_NICKNAME_CHANGES - 1);
            user.setNicknameModifiedAt(modifiedAt);
            siteUserRepository.save(user);

            // when & then
            assertThatCode(() -> myPageService.updateMyPageInfo(user.getId(), imageFile, "nickname12"))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(createExpectedErrorMessage(modifiedAt));
        }
    }

    private int createLikedUnivApplyInfos(SiteUser testUser) {
        LikedUnivApplyInfo likedUnivApplyInfo1 = new LikedUnivApplyInfo(null, univApplyInfoFixture.괌대학_A_지원_정보().getId(), testUser.getId());
        LikedUnivApplyInfo likedUnivApplyInfo2 = new LikedUnivApplyInfo(null, univApplyInfoFixture.메이지대학_지원_정보().getId(), testUser.getId());
        LikedUnivApplyInfo likedUnivApplyInfo3 = new LikedUnivApplyInfo(null, univApplyInfoFixture.코펜하겐IT대학_지원_정보().getId(), testUser.getId());

        likedUnivApplyInfoRepository.save(likedUnivApplyInfo1);
        likedUnivApplyInfoRepository.save(likedUnivApplyInfo2);
        likedUnivApplyInfoRepository.save(likedUnivApplyInfo3);
        return likedUnivApplyInfoRepository.countBySiteUserId(testUser.getId());
    }

    private MockMultipartFile createValidImageFile() {
        return new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    private String createExpectedErrorMessage(LocalDateTime modifiedAt) {
        String formatLastModifiedAt = String.format(
                "(마지막 수정 시간 : %s)",
                NICKNAME_LAST_CHANGE_DATE_FORMAT.format(modifiedAt)
        );
        return CAN_NOT_CHANGE_NICKNAME_YET.getMessage() + " : " + formatLastModifiedAt;
    }

    private SiteUser createSiteUserWithCustomProfile() {
        return siteUserFixtureBuilder.siteUser()
                .email("customProfile@example.com")
                .authType(AuthType.EMAIL)
                .nickname("커스텀프로필")
                .profileImageUrl("profile/profileImageUrl")
                .role(Role.MENTEE)
                .password("customPassword123")
                .create();
    }
}
