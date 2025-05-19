package com.example.solidconnection.university.service;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.LikedUniversity;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.LikeResultResponse;
import com.example.solidconnection.university.fixture.UniversityInfoForApplyFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.example.solidconnection.custom.exception.ErrorCode.ALREADY_LIKED_UNIVERSITY;
import static com.example.solidconnection.custom.exception.ErrorCode.NOT_LIKED_UNIVERSITY;
import static com.example.solidconnection.custom.exception.ErrorCode.UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND;
import static com.example.solidconnection.university.service.UniversityLikeService.LIKE_CANCELED_MESSAGE;
import static com.example.solidconnection.university.service.UniversityLikeService.LIKE_SUCCESS_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestContainerSpringBootTest
@DisplayName("대학교 좋아요 서비스 테스트")
class UniversityLikeServiceTest {

    @Autowired
    private UniversityLikeService universityLikeService;

    @Autowired
    private LikedUniversityRepository likedUniversityRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private UniversityInfoForApplyFixture universityInfoForApplyFixture;

    private SiteUser user;
    private UniversityInfoForApply 괌대학_A_지원_정보;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
        괌대학_A_지원_정보 = universityInfoForApplyFixture.괌대학_A_지원_정보();
    }

    @Nested
    class 대학_좋아요를_등록한다 {

        @Test
        void 성공적으로_좋아요를_등록한다() {
            // when
            LikeResultResponse response = universityLikeService.likeUniversity(user, 괌대학_A_지원_정보.getId());

            // then
            assertAll(
                    () -> assertThat(response.result()).isEqualTo(LIKE_SUCCESS_MESSAGE),
                    () -> assertThat(likedUniversityRepository.findBySiteUserAndUniversityInfoForApply(
                            user, 괌대학_A_지원_정보
                    )).isPresent()
            );
        }

        @Test
        void 이미_좋아요한_대학이면_예외_응답을_반환한다() {
            // given
            saveLikedUniversity(user, 괌대학_A_지원_정보);

            // when & then
            assertThatCode(() -> universityLikeService.likeUniversity(user, 괌대학_A_지원_정보.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ALREADY_LIKED_UNIVERSITY.getMessage());
        }
    }

    @Nested
    class 대학_좋아요를_취소한다 {

        @Test
        void 성공적으로_좋아요를_취소한다() {
            // given
            saveLikedUniversity(user, 괌대학_A_지원_정보);

            // when
            LikeResultResponse response = universityLikeService.cancelLikeUniversity(user, 괌대학_A_지원_정보.getId());

            // then
            assertAll(
                    () -> assertThat(response.result()).isEqualTo(LIKE_CANCELED_MESSAGE),
                    () -> assertThat(likedUniversityRepository.findBySiteUserAndUniversityInfoForApply(
                            user, 괌대학_A_지원_정보
                    )).isEmpty()
            );
        }

        @Test
        void 좋아요하지_않은_대학이면_예외_응답을_반환한다() {
            // when & then
            assertThatCode(() -> universityLikeService.cancelLikeUniversity(user, 괌대학_A_지원_정보.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(NOT_LIKED_UNIVERSITY.getMessage());
        }
    }

    @Test
    void 존재하지_않는_대학_좋아요_시도하면_예외_응답을_반환한다() {
        // given
        Long invalidUniversityId = 9999L;

        // when & then
        assertThatCode(() -> universityLikeService.likeUniversity(user, invalidUniversityId))
                .isInstanceOf(CustomException.class)
                .hasMessage(UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND.getMessage());
    }

    @Test
    void 좋아요한_대학인지_확인한다() {
        // given
        saveLikedUniversity(user, 괌대학_A_지원_정보);

        // when
        IsLikeResponse response = universityLikeService.getIsLiked(user, 괌대학_A_지원_정보.getId());

        // then
        assertThat(response.isLike()).isTrue();
    }

    @Test
    void 좋아요하지_않은_대학인지_확인한다() {
        // when
        IsLikeResponse response = universityLikeService.getIsLiked(user, 괌대학_A_지원_정보.getId());

        // then
        assertThat(response.isLike()).isFalse();
    }

    @Test
    void 존재하지_않는_대학의_좋아요_여부를_조회하면_예외_응답을_반환한다() {
        // given
        Long invalidUniversityId = 9999L;

        // when & then
        assertThatCode(() -> universityLikeService.getIsLiked(user, invalidUniversityId))
                .isInstanceOf(CustomException.class)
                .hasMessage(UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND.getMessage());
    }

    private void saveLikedUniversity(SiteUser siteUser, UniversityInfoForApply universityInfoForApply) {
        LikedUniversity likedUniversity = LikedUniversity.builder()
                .siteUser(siteUser)
                .universityInfoForApply(universityInfoForApply)
                .build();
        likedUniversityRepository.save(likedUniversity);
    }
}
