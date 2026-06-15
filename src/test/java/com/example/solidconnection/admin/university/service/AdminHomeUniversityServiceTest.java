package com.example.solidconnection.admin.university.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.admin.university.dto.AdminHomeUniversityCreateRequest;
import com.example.solidconnection.admin.university.dto.AdminHomeUniversityResponse;
import com.example.solidconnection.admin.university.dto.AdminHomeUniversityUpdateRequest;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.fixture.HomeUniversityFixture;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import com.example.solidconnection.university.repository.HomeUniversityRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("협정 대학 관리자 서비스 테스트")
class AdminHomeUniversityServiceTest {

    @Autowired
    private AdminHomeUniversityService adminHomeUniversityService;

    @Autowired
    private HomeUniversityRepository homeUniversityRepository;

    @Autowired
    private HomeUniversityFixture homeUniversityFixture;

    @Autowired
    private UnivApplyInfoFixture univApplyInfoFixture;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Nested
    class 전체_협정대학_조회 {

        @Test
        void 협정대학이_없으면_빈_목록을_반환한다() {
            // when
            List<AdminHomeUniversityResponse> responses = adminHomeUniversityService.getAllHomeUniversities();

            // then
            assertThat(responses).isEmpty();
        }

        @Test
        void 저장된_모든_협정대학을_반환한다() {
            // given
            HomeUniversity homeUniversity1 = homeUniversityFixture.인하대학교();
            HomeUniversity homeUniversity2 = homeUniversityFixture.인천대학교();

            // when
            List<AdminHomeUniversityResponse> responses = adminHomeUniversityService.getAllHomeUniversities();

            // then
            assertThat(responses)
                    .hasSize(2)
                    .extracting(AdminHomeUniversityResponse::name)
                    .containsExactlyInAnyOrder(homeUniversity1.getName(), homeUniversity2.getName());
        }
    }

    @Nested
    class 협정대학_단건_조회 {

        @Test
        void 존재하는_협정대학을_조회하면_성공한다() {
            // given
            HomeUniversity homeUniversity = homeUniversityFixture.인하대학교();

            // when
            AdminHomeUniversityResponse response = adminHomeUniversityService.getHomeUniversity(homeUniversity.getId());

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(homeUniversity.getId()),
                    () -> assertThat(response.name()).isEqualTo(homeUniversity.getName()),
                    () -> assertThat(response.maxChoiceCount()).isEqualTo(homeUniversity.getMaxChoiceCount()),
                    () -> assertThat(response.emailDomain()).isEqualTo(homeUniversity.getEmailDomain())
            );
        }

        @Test
        void 존재하지_않는_협정대학을_조회하면_예외가_발생한다() {
            // when & then
            assertThatCode(() -> adminHomeUniversityService.getHomeUniversity(999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.HOME_UNIVERSITY_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 협정대학_생성 {

        @Test
        void 유효한_요청으로_협정대학을_생성하면_성공한다() {
            // given
            AdminHomeUniversityCreateRequest request = new AdminHomeUniversityCreateRequest("인하대학교", 3, "inha.edu");

            // when
            AdminHomeUniversityResponse response = adminHomeUniversityService.createHomeUniversity(request);

            // then
            HomeUniversity saved = homeUniversityRepository.findById(response.id()).orElseThrow();
            assertAll(
                    () -> assertThat(response.name()).isEqualTo("인하대학교"),
                    () -> assertThat(response.maxChoiceCount()).isEqualTo(3),
                    () -> assertThat(response.emailDomain()).isEqualTo("inha.edu"),
                    () -> assertThat(saved.getName()).isEqualTo("인하대학교"),
                    () -> assertThat(saved.getMaxChoiceCount()).isEqualTo(3),
                    () -> assertThat(saved.getEmailDomain()).isEqualTo("inha.edu")
            );
        }

        @Test
        void 이미_존재하는_이름으로_생성하면_예외가_발생한다() {
            // given
            homeUniversityFixture.인하대학교();
            AdminHomeUniversityCreateRequest request = new AdminHomeUniversityCreateRequest("인하대학교", 3, "other.ac.kr");

            // when & then
            assertThatCode(() -> adminHomeUniversityService.createHomeUniversity(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.HOME_UNIVERSITY_ALREADY_EXISTS.getMessage());
        }

        @Test
        void 이미_존재하는_이메일_도메인으로_생성하면_예외가_발생한다() {
            // given
            homeUniversityFixture.인하대학교();
            AdminHomeUniversityCreateRequest request = new AdminHomeUniversityCreateRequest("연세대학교", 3, "inha.edu");

            // when & then
            assertThatCode(() -> adminHomeUniversityService.createHomeUniversity(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.HOME_UNIVERSITY_EMAIL_DOMAIN_ALREADY_EXISTS.getMessage());
        }
    }

    @Nested
    class 협정대학_수정 {

        @Test
        void 유효한_요청으로_협정대학을_수정하면_성공한다() {
            // given
            HomeUniversity homeUniversity = homeUniversityFixture.인하대학교();
            AdminHomeUniversityUpdateRequest request = new AdminHomeUniversityUpdateRequest("연세대학교", 5, "yonsei.ac.kr");

            // when
            AdminHomeUniversityResponse response = adminHomeUniversityService.updateHomeUniversity(homeUniversity.getId(), request);

            // then
            HomeUniversity updated = homeUniversityRepository.findById(homeUniversity.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(response.name()).isEqualTo("연세대학교"),
                    () -> assertThat(response.maxChoiceCount()).isEqualTo(5),
                    () -> assertThat(response.emailDomain()).isEqualTo("yonsei.ac.kr"),
                    () -> assertThat(updated.getName()).isEqualTo("연세대학교"),
                    () -> assertThat(updated.getMaxChoiceCount()).isEqualTo(5),
                    () -> assertThat(updated.getEmailDomain()).isEqualTo("yonsei.ac.kr")
            );
        }

        @Test
        void 존재하지_않는_협정대학을_수정하면_예외가_발생한다() {
            // given
            AdminHomeUniversityUpdateRequest request = new AdminHomeUniversityUpdateRequest("연세대학교", 3, "yonsei.ac.kr");

            // when & then
            assertThatCode(() -> adminHomeUniversityService.updateHomeUniversity(999L, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.HOME_UNIVERSITY_NOT_FOUND.getMessage());
        }

        @Test
        void 다른_협정대학의_이름으로_수정하면_예외가_발생한다() {
            // given
            homeUniversityFixture.인하대학교();
            HomeUniversity other = homeUniversityRepository.save(new HomeUniversity(null, "연세대학교", 3, "yonsei.ac.kr"));
            AdminHomeUniversityUpdateRequest request = new AdminHomeUniversityUpdateRequest("인하대학교", 3, "yonsei.ac.kr");

            // when & then
            assertThatCode(() -> adminHomeUniversityService.updateHomeUniversity(other.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.HOME_UNIVERSITY_ALREADY_EXISTS.getMessage());
        }

        @Test
        void 다른_협정대학의_이메일_도메인으로_수정하면_예외가_발생한다() {
            // given
            homeUniversityFixture.인하대학교();
            HomeUniversity other = homeUniversityRepository.save(new HomeUniversity(null, "연세대학교", 3, "yonsei.ac.kr"));
            AdminHomeUniversityUpdateRequest request = new AdminHomeUniversityUpdateRequest("연세대학교", 3, "inha.edu");

            // when & then
            assertThatCode(() -> adminHomeUniversityService.updateHomeUniversity(other.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.HOME_UNIVERSITY_EMAIL_DOMAIN_ALREADY_EXISTS.getMessage());
        }

        @Test
        void 같은_이름으로_수정하면_성공한다() {
            // given
            HomeUniversity homeUniversity = homeUniversityFixture.인하대학교();
            AdminHomeUniversityUpdateRequest request = new AdminHomeUniversityUpdateRequest("인하대학교", 3, "inha.edu");

            // when
            AdminHomeUniversityResponse response = adminHomeUniversityService.updateHomeUniversity(homeUniversity.getId(), request);

            // then
            assertThat(response.name()).isEqualTo("인하대학교");
        }
    }

    @Nested
    class 협정대학_삭제 {

        @Test
        void 참조가_없는_협정대학을_삭제하면_성공한다() {
            // given
            HomeUniversity homeUniversity = homeUniversityFixture.인하대학교();

            // when
            adminHomeUniversityService.deleteHomeUniversity(homeUniversity.getId());

            // then
            assertThat(homeUniversityRepository.findById(homeUniversity.getId())).isEmpty();
        }

        @Test
        void 존재하지_않는_협정대학을_삭제하면_예외가_발생한다() {
            // when & then
            assertThatCode(() -> adminHomeUniversityService.deleteHomeUniversity(999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.HOME_UNIVERSITY_NOT_FOUND.getMessage());
        }

        @Test
        void UnivApplyInfo가_참조하는_협정대학을_삭제하면_예외가_발생한다() {
            // given
            HomeUniversity homeUniversity = homeUniversityFixture.인하대학교();
            univApplyInfoFixture.괌대학_A_지원_정보(1L);

            // when & then
            assertThatCode(() -> adminHomeUniversityService.deleteHomeUniversity(homeUniversity.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.HOME_UNIVERSITY_HAS_REFERENCES.getMessage());
        }

        @Test
        void SiteUser가_참조하는_협정대학을_삭제하면_예외가_발생한다() {
            // given
            HomeUniversity homeUniversity = homeUniversityFixture.인하대학교();
            SiteUser siteUser = siteUserFixture.국내_대학_정보_소지_사용자(homeUniversity.getId());

            // when & then
            assertThatCode(() -> adminHomeUniversityService.deleteHomeUniversity(homeUniversity.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.HOME_UNIVERSITY_HAS_REFERENCES.getMessage());
        }
    }
}
