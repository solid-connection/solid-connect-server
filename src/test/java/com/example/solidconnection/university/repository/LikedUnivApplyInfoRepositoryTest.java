package com.example.solidconnection.university.repository;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.LikedUnivApplyInfo;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@TestContainerSpringBootTest
@DisplayName("대학교 좋아요 레파지토리 테스트")
public class LikedUnivApplyInfoRepositoryTest {

    @Autowired
    private LikedUnivApplyInfoRepository likedUnivApplyInfoRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private UnivApplyInfoFixture univApplyInfoFixture;

    @Nested
    class 사용자와_좋아요한_대학은_복합_유니크_제약조건을_갖는다 {

        @Test
        void 같은_사용자가_같은_대학에_중복으로_좋아요하면_예외가_발생한다() {
            // given
            SiteUser user = siteUserFixture.사용자();
            UnivApplyInfo univApplyInfo = univApplyInfoFixture.괌대학_A_지원_정보();

            LikedUnivApplyInfo firstLike = createLikedUnivApplyInfo(user, univApplyInfo);
            likedUnivApplyInfoRepository.save(firstLike);

            LikedUnivApplyInfo secondLike = createLikedUnivApplyInfo(user, univApplyInfo);

            // when & then
            assertThatCode(() -> likedUnivApplyInfoRepository.save(secondLike))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void 다른_사용자가_같은_대학에_좋아요하면_정상_저장된다() {
            // given
            SiteUser user1 = siteUserFixture.사용자(1, "user1");
            SiteUser user2 = siteUserFixture.사용자(2, "user2");
            UnivApplyInfo univApplyInfo = univApplyInfoFixture.괌대학_A_지원_정보();

            LikedUnivApplyInfo firstLike = createLikedUnivApplyInfo(user1, univApplyInfo);
            likedUnivApplyInfoRepository.save(firstLike);

            LikedUnivApplyInfo secondLike = createLikedUnivApplyInfo(user2, univApplyInfo);

            // when & then
            assertThatCode(() -> likedUnivApplyInfoRepository.save(secondLike)).doesNotThrowAnyException();
        }

        @Test
        void 같은_사용자가_다른_대학에_좋아요하면_정상_저장된다() {
            // given
            SiteUser user = siteUserFixture.사용자();
            UnivApplyInfo univApplyInfo1 = univApplyInfoFixture.괌대학_A_지원_정보();
            UnivApplyInfo univApplyInfo2 = univApplyInfoFixture.메이지대학_지원_정보();

            LikedUnivApplyInfo firstLike = createLikedUnivApplyInfo(user, univApplyInfo1);
            likedUnivApplyInfoRepository.save(firstLike);

            LikedUnivApplyInfo secondLike = createLikedUnivApplyInfo(user, univApplyInfo2);

            // when & then
            assertThatCode(() -> likedUnivApplyInfoRepository.save(secondLike)).doesNotThrowAnyException();
        }
    }

    private LikedUnivApplyInfo createLikedUnivApplyInfo(SiteUser siteUser, UnivApplyInfo univApplyInfo) {
        return LikedUnivApplyInfo.builder()
                .siteUserId(siteUser.getId())
                .univApplyInfoId(univApplyInfo.getId())
                .build();
    }
}
