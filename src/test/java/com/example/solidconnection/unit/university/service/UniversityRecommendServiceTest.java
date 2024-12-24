package com.example.solidconnection.unit.university.service;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.*;
import com.example.solidconnection.university.domain.*;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import com.example.solidconnection.university.dto.UniversityRecommendsResponse;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.service.GeneralRecommendUniversities;
import com.example.solidconnection.university.service.UniversityRecommendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static java.util.stream.IntStream.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("대학교 추천 서비스 테스트")
class UniversityRecommendServiceTest {

    @InjectMocks
    private UniversityRecommendService universityRecommendService;

    @Mock
    private UniversityInfoForApplyRepository universityInfoForApplyRepository;

    @Mock
    private GeneralRecommendUniversities generalRecommendUniversities;

    @Mock
    private SiteUserRepository siteUserRepository;

    private SiteUser testUser;
    private List<UniversityInfoForApply> testUniversities;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(universityRecommendService, "term", "2025-1-a");
        testUser = createTestUser();
        testUniversities = createTestUniversities();
    }

    @Test
    @DisplayName("일반 추천 목록을 정상적으로 반환한다")
    void getGeneralRecommendsShouldReturnShuffledAndConvertedList() {
        // given
        List<UniversityInfoForApply> generalUniversities =
                range(0, UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM)
                        .mapToObj(this::createUniversityInfo)
                        .toList();
        when(generalRecommendUniversities.getRecommendUniversities())
                .thenReturn(generalUniversities);

        // when
        UniversityRecommendsResponse response = universityRecommendService.getGeneralRecommends();

        // then
        assertThat(response.recommendedUniversities()).isNotNull();
        assertThat(response.recommendedUniversities())
                .hasSize(UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM);
        response.recommendedUniversities().forEach(preview -> {
            assertThat(preview).isInstanceOf(UniversityInfoForApplyPreviewResponse.class);
            assertThat(preview.studentCapacity()).isEqualTo(3);
            assertThat(preview.region()).isEqualTo("유럽권");
            assertThat(preview.country()).isEqualTo("프랑스");
        });
        assertThat(response.recommendedUniversities())
                .extracting("koreanName")
                .isNotEqualTo(generalUniversities.stream()
                        .map(UniversityInfoForApply::getKoreanName)
                        .toList());
        // verify
        verify(generalRecommendUniversities).getRecommendUniversities();
    }

    @Test
    @DisplayName("개인화 추천 목록을 정상적으로 반환한다")
    void getPersonalRecommendsShouldReturnPersonalizedAndShuffledList() {

        // given
        String email = "test@example.com";
        List<UniversityInfoForApply> personalRecommends = new ArrayList<>(
                range(0, UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM)
                        .mapToObj(this::createUniversityInfo)
                        .toList()
        );


        when(siteUserRepository.getByEmail(email)).thenReturn(testUser);
        when(universityInfoForApplyRepository
                .findUniversityInfoForAppliesBySiteUsersInterestedCountryOrRegionAndTerm(any(), anyString()))
                .thenReturn(personalRecommends);

        // when
        List<UniversityInfoForApply> originalList = new ArrayList<>(personalRecommends);
        UniversityRecommendsResponse response = universityRecommendService.getPersonalRecommends(email);

        // then
        assertThat(response.recommendedUniversities()).isNotNull();
        assertThat(response.recommendedUniversities())
                .hasSize(UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM);
        assertThat(response.recommendedUniversities())
                .extracting("koreanName")
                .containsExactlyInAnyOrderElementsOf(originalList.stream()
                        .map(UniversityInfoForApply::getKoreanName)
                        .toList());
        List<String> shuffledOrder = response.recommendedUniversities().stream()
                .map(UniversityInfoForApplyPreviewResponse::koreanName)
                .toList();
        assertThat(shuffledOrder)
                .isNotEqualTo(originalList.stream()
                        .map(UniversityInfoForApply::getKoreanName)
                        .toList());

        // verify
        verify(siteUserRepository).getByEmail(email);
        verify(universityInfoForApplyRepository)
                .findUniversityInfoForAppliesBySiteUsersInterestedCountryOrRegionAndTerm(any(), anyString());
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 경우 예외를 반환한다")
    void getPersonalRecommendsWithInvalidUserShouldThrowException() {
        // given
        String email = "invalid@example.com";
        when(siteUserRepository.getByEmail(email))
                .thenThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> universityRecommendService.getPersonalRecommends(email));
        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_FOUND.getCode());

        // verify
        verify(siteUserRepository).getByEmail(email);
    }

    @Test
    @DisplayName("개인화 추천이 부족할 경우 일반 추천으로 보충한다")
    void getPersonalRecommendsWithInsufficientRecommendsShouldSupplementWithGeneral() {
        // given
        String email = "test@example.com";
        List<UniversityInfoForApply> personalRecommends = new ArrayList<>();
        personalRecommends.add(testUniversities.get(0));
        List<UniversityInfoForApply> generalRecommends = createAdditionalUniversities();
        when(siteUserRepository.getByEmail(email)).thenReturn(testUser);
        when(universityInfoForApplyRepository
                .findUniversityInfoForAppliesBySiteUsersInterestedCountryOrRegionAndTerm(any(), anyString()))
                .thenReturn(personalRecommends);
        when(generalRecommendUniversities.getRecommendUniversities())
                .thenReturn(generalRecommends);

        // when
        UniversityRecommendsResponse response = universityRecommendService.getPersonalRecommends(email);

        // then
        assertThat(response.recommendedUniversities()).isNotNull();
        assertThat(response.recommendedUniversities())
                .hasSize(UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM);
        List<String> allKoreanNames = response.recommendedUniversities().stream()
                .map(UniversityInfoForApplyPreviewResponse::koreanName)
                .toList();
        assertThat(allKoreanNames).doesNotHaveDuplicates();
        response.recommendedUniversities().forEach(preview -> {
            assertThat(preview).isInstanceOf(UniversityInfoForApplyPreviewResponse.class);
            assertThat(preview.studentCapacity()).isEqualTo(3);
            assertThat(preview.region()).isEqualTo("유럽권");
            assertThat(preview.country()).isEqualTo("프랑스");
        });

        // verify
        verify(siteUserRepository).getByEmail(email);
        verify(universityInfoForApplyRepository)
                .findUniversityInfoForAppliesBySiteUsersInterestedCountryOrRegionAndTerm(any(), anyString());
        verify(generalRecommendUniversities).getRecommendUniversities();
    }

    private SiteUser createTestUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-10-21",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
    }

    private List<UniversityInfoForApply> createTestUniversities() {
        List<UniversityInfoForApply> universities = new ArrayList<>();
        for(int i = 0; i < UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM + 2; i++) {
            universities.add(createUniversityInfo(i));
        }
        return universities;
    }

    private List<UniversityInfoForApply> createAdditionalUniversities() {
        List<UniversityInfoForApply> universities = new ArrayList<>();
        for(int i = UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM; i < UniversityRecommendService.RECOMMEND_UNIVERSITY_NUM * 2; i++) {
            universities.add(createUniversityInfo(i));
        }
        return universities;
    }

    private UniversityInfoForApply createUniversityInfo(int index) {
        Region region = new Region("EUROPE", "유럽권");
        Country country = new Country("FR", "프랑스", region);

        University university = new University(
                (long) index,
                "University" + index,
                "University" + index,
                "univ" + index,
                "https://example.com/life" + index,
                "https://example.com/courses" + index,
                "https://example.com/accommodation" + index,
                "https://example.com/logo" + index,
                "https://example.com/background" + index,
                "details" + index,
                country,
                region
        );

        Set<LanguageRequirement> requirements = new HashSet<>();
        requirements.add(new LanguageRequirement(
                (long) index,
                LanguageTestType.TOEFL_IBT,
                "90",
                null
        ));

        return new UniversityInfoForApply(
                (long) index,
                "2025-1-a",
                "University" + index,
                3,
                TuitionFeeType.HOME_UNIVERSITY_PAYMENT,
                SemesterAvailableForDispatch.IRRELEVANT,
                "4학기",
                "어학 요구사항" + index,
                "3.0/4.5",
                "4.5",
                "지원 상세" + index,
                "전공 상세" + index,
                "기숙사 상세" + index,
                "영어 강좌 상세" + index,
                "기타 상세" + index,
                requirements,
                university
        );
    }
}