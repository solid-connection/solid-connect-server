package com.example.solidconnection.siteuser.repository;

import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatCode;

// @TestContainerDataJpaTest + @Import 쓰기 vs @TestContainerSpringBootTest 검토 필요
@TestContainerSpringBootTest
//@Import({SiteUserFixture.class, SiteUserFixtureBuilder.class, PasswordEncoder.class})
@DisplayName("유저 레포지토리 테스트")
class SiteUserRepositoryTest {

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Nested
    class 이메일과_인증_유형이_동일한_사용자는_저장할_수_없다 {

        @Test
        void 이메일과_인증_유형이_동일한_사용자를_저장하면_예외_응답을_반환한다() {
            // given
            siteUserFixture.사용자("email", AuthType.KAKAO);

            // when, then
            assertThatCode(() -> siteUserFixture.사용자("email", AuthType.KAKAO))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void 이메일이_같더라도_인증_유형이_다른_사용자는_정상_저장한다() {
            // given
            siteUserFixture.사용자("email", AuthType.KAKAO);

            // when, then
            assertThatCode(() -> siteUserFixture.사용자("email", AuthType.APPLE))
                    .doesNotThrowAnyException();
        }
    }
}
