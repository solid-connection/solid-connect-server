package com.example.solidconnection.university.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.university.repository.LikedUnivApplyInfoRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.LikedUnivApplyInfo;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.LikeResultResponse;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_LIKED_UNIV_APPLY_INFO;
import static com.example.solidconnection.common.exception.ErrorCode.NOT_LIKED_UNIV_APPLY_INFO;
import static com.example.solidconnection.common.exception.ErrorCode.UNIV_APPLY_INFO_NOT_FOUND;
import static com.example.solidconnection.university.service.UnivApplyInfoLikeService.LIKE_CANCELED_MESSAGE;
import static com.example.solidconnection.university.service.UnivApplyInfoLikeService.LIKE_SUCCESS_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestContainerSpringBootTest
@DisplayName("대학 지원 정보 좋아요 서비스 테스트")
class UnivApplyInfoLikeServiceTest {

    @Autowired
    private UnivApplyInfoLikeService univApplyInfoLikeService;

    @Autowired
    private LikedUnivApplyInfoRepository likedUnivApplyInfoRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private UnivApplyInfoFixture univApplyInfoFixture;

    private SiteUser user;
    private UnivApplyInfo 괌대학_A_지원_정보;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
        괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보();
    }

    @Nested
    class 대학_지원_정보_좋아요를_등록한다 {

        @Test
        void 성공적으로_좋아요를_등록한다() {
            // when
            LikeResultResponse response = univApplyInfoLikeService.addUnivApplyInfoLike(user, 괌대학_A_지원_정보.getId());

            // then
            assertAll(
                    () -> assertThat(response.result()).isEqualTo(LIKE_SUCCESS_MESSAGE),
                    () -> assertThat(likedUnivApplyInfoRepository.findBySiteUserIdAndUnivApplyInfoId(
                            user.getId(), 괌대학_A_지원_정보.getId()
                    )).isPresent()
            );
        }

        @Test
        void 이미_좋아요했으면_예외_응답을_반환한다() {
            // given
            saveLikedUniversity(user, 괌대학_A_지원_정보);

            // when & then
            assertThatCode(() -> univApplyInfoLikeService.addUnivApplyInfoLike(user, 괌대학_A_지원_정보.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ALREADY_LIKED_UNIV_APPLY_INFO.getMessage());
        }
    }

    @Nested
    class 대학_지원_정보_좋아요를_취소한다 {

        @Test
        void 성공적으로_좋아요를_취소한다() {
            // given
            saveLikedUniversity(user, 괌대학_A_지원_정보);

            // when
            LikeResultResponse response = univApplyInfoLikeService.cancelUnivApplyInfoLike(user, 괌대학_A_지원_정보.getId());

            // then
            assertAll(
                    () -> assertThat(response.result()).isEqualTo(LIKE_CANCELED_MESSAGE),
                    () -> assertThat(likedUnivApplyInfoRepository.findBySiteUserIdAndUnivApplyInfoId(
                            user.getId(), 괌대학_A_지원_정보.getId()
                    )).isEmpty()
            );
        }

        @Test
        void 좋아요하지_않았으면_예외_응답을_반환한다() {
            // when & then
            assertThatCode(() -> univApplyInfoLikeService.cancelUnivApplyInfoLike(user, 괌대학_A_지원_정보.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(NOT_LIKED_UNIV_APPLY_INFO.getMessage());
        }
    }

    @Test
    void 존재하지_않는_지원_정보에_좋아요_시도하면_예외_응답을_반환한다() {
        // given
        Long invalidUnivApplyInfoId = 9999L;

        // when & then
        assertThatCode(() -> univApplyInfoLikeService.addUnivApplyInfoLike(user, invalidUnivApplyInfoId))
                .isInstanceOf(CustomException.class)
                .hasMessage(UNIV_APPLY_INFO_NOT_FOUND.getMessage());
    }

    @Test
    void 좋아요한_대학_지원_정보인지_확인한다() {
        // given
        saveLikedUniversity(user, 괌대학_A_지원_정보);

        // when
        IsLikeResponse response = univApplyInfoLikeService.isUnivApplyInfoLiked(user, 괌대학_A_지원_정보.getId());

        // then
        assertThat(response.isLike()).isTrue();
    }

    @Test
    void 좋아요하지_않은_대학_지원_정보인지_확인한다() {
        // when
        IsLikeResponse response = univApplyInfoLikeService.isUnivApplyInfoLiked(user, 괌대학_A_지원_정보.getId());

        // then
        assertThat(response.isLike()).isFalse();
    }

    @Test
    void 존재하지_않는_대학_지원_정보의_좋아요_여부를_조회하면_예외_응답을_반환한다() {
        // given
        Long invalidUnivApplyInfoId = 9999L;

        // when & then
        assertThatCode(() -> univApplyInfoLikeService.isUnivApplyInfoLiked(user, invalidUnivApplyInfoId))
                .isInstanceOf(CustomException.class)
                .hasMessage(UNIV_APPLY_INFO_NOT_FOUND.getMessage());
    }

    private void saveLikedUniversity(SiteUser siteUser, UnivApplyInfo univApplyInfo) {
        LikedUnivApplyInfo likedUnivApplyInfo = LikedUnivApplyInfo.builder()
                .siteUserId(siteUser.getId())
                .univApplyInfoId(univApplyInfo.getId())
                .build();
        likedUnivApplyInfoRepository.save(likedUnivApplyInfo);
    }
}
