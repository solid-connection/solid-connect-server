package com.example.solidconnection.siteuser.repository;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.ExchangeStatus;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.support.TestContainerDataJpaTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

@TestContainerDataJpaTest
class SiteUserRepositoryTest {

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Nested
    class 이메일과_인증_유형이_동일한_사용자는_저장할_수_없다 {

        @Test
        void 이메일과_인증_유형이_동일한_사용자를_저장하면_예외_응답을_반환한다() {
            // given
            SiteUser user1 = createSiteUser("email", "nickname1", AuthType.KAKAO);
            SiteUser user2 = createSiteUser("email", "nickname2", AuthType.KAKAO);
            siteUserRepository.save(user1);

            // when, then
            assertThatCode(() -> siteUserRepository.save(user2))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void 이메일이_같더라도_인증_유형이_다른_사용자는_정상_저장한다() {
            // given
            SiteUser user1 = createSiteUser("email", "nickname1", AuthType.KAKAO);
            SiteUser user2 = createSiteUser("email", "nickname2", AuthType.APPLE);
            siteUserRepository.save(user1);

            // when, then
            assertThatCode(() -> siteUserRepository.save(user2))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class 닉네임은_중복될_수_없다 {

        @Test
        void 중복된_닉네임으로_사용자를_저장하면_예외_응답을_반환한다() {
            // given
            SiteUser user1 = createSiteUser("email1", "nickname", AuthType.KAKAO);
            SiteUser user2 = createSiteUser("email2", "nickname", AuthType.KAKAO);
            siteUserRepository.save(user1);

            // when, then
            assertThatCode(() -> siteUserRepository.saveAndFlush(user2))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void 중복된_닉네임으로_변경하면_예외_응답을_반환한다() {
            // given
            SiteUser user1 = createSiteUser("email1", "nickname1", AuthType.KAKAO);
            SiteUser user2 = createSiteUser("email2", "nickname2", AuthType.KAKAO);
            siteUserRepository.save(user1);
            siteUserRepository.save(user2);

            // when
            user2.setNickname("nickname1");

            // then
            assertThatCode(() -> siteUserRepository.saveAndFlush(user2))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    private SiteUser createSiteUser(String email, String nickname, AuthType authType) {
        return new SiteUser(
                email,
                nickname,
                "profileImageUrl",
                ExchangeStatus.CONSIDERING,
                Role.MENTEE,
                authType
        );
    }
}
