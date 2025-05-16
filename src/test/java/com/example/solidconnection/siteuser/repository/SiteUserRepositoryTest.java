package com.example.solidconnection.siteuser.repository;

import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.support.TestContainerDataJpaTest;
import com.example.solidconnection.siteuser.domain.PreparationStatus;
import com.example.solidconnection.siteuser.domain.Role;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatCode;

@TestContainerDataJpaTest
class SiteUserRepositoryTest {

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Nested
    class 이메일과_인증_유형이_동일한_사용자는_저장할_수_없다 {

        @Test
        void 이메일과_인증_유형이_동일한_사용자를_저장하면_예외_응답을_반환한다() {
            // given
            SiteUser user1 = createSiteUser("email", AuthType.KAKAO);
            SiteUser user2 = createSiteUser("email", AuthType.KAKAO);
            siteUserRepository.save(user1);

            // when, then
            assertThatCode(() -> siteUserRepository.save(user2))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void 이메일이_같더라도_인증_유형이_다른_사용자는_정상_저장한다() {
            // given
            SiteUser user1 = createSiteUser("email", AuthType.KAKAO);
            SiteUser user2 = createSiteUser("email", AuthType.APPLE);
            siteUserRepository.save(user1);

            // when, then
            assertThatCode(() -> siteUserRepository.save(user2))
                    .doesNotThrowAnyException();
        }
    }

    private SiteUser createSiteUser(String email, AuthType authType) {
        return new SiteUser(
                email,
                "nickname",
                "profileImageUrl",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                authType
        );
    }
}
