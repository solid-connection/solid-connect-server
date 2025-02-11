package com.example.solidconnection.e2e;

import com.example.solidconnection.auth.service.AuthTokenProvider;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.domain.LikedUniversity;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.LikeResultResponse;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.example.solidconnection.e2e.DynamicFixture.createLikedUniversity;
import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserByEmail;
import static com.example.solidconnection.e2e.DynamicFixture.createUniversityForApply;
import static com.example.solidconnection.university.service.UniversityLikeService.LIKE_SUCCESS_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("대학교 좋아요 테스트")
class UniversityLikeTest extends UniversityDataSetUpEndToEndTest {

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private UniversityInfoForApplyRepository universityInfoForApplyRepository;

    @Autowired
    private LikedUniversityRepository likedUniversityRepository;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    private String accessToken;
    private SiteUser siteUser;

    @BeforeEach
    public void setUpUserAndToken() {
        // setUp - 회원 정보 저장
        siteUser = createSiteUserByEmail("email@email.com");
        siteUserRepository.save(siteUser);

        // setUp - 엑세스 토큰 생성과 리프레시 토큰 생성 및 저장
        accessToken = authTokenProvider.generateAccessToken(siteUser);
        authTokenProvider.generateAndSaveRefreshToken(siteUser);
    }

    @Test
    void 좋아요를_한_대학을_조회한다() {
        // setUp - 대학교 좋아요 저장
        UniversityInfoForApply differentTermUniversityInfoForApply =
                createUniversityForApply(term + " 추가 지원", 영미권_미국_괌대학, null);
        universityInfoForApplyRepository.save(differentTermUniversityInfoForApply);
        likedUniversityRepository.saveAll(Set.of(
                createLikedUniversity(siteUser, 괌대학_A_지원_정보),
                createLikedUniversity(siteUser, differentTermUniversityInfoForApply)
        ));

        // request - 요청
        List<UniversityInfoForApplyPreviewResponse> wishUniversities = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .log().all()
                .get("/universities/like")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList(".", UniversityInfoForApplyPreviewResponse.class);

        List<Long> wishUniversitiesId = wishUniversities.stream()
                .map(UniversityInfoForApplyPreviewResponse::id)
                .toList();
        assertThat(wishUniversitiesId)
                .as("좋아요한 대학교를 지원 시기와 관계 없이 불러온다.")
                .containsExactlyInAnyOrder(괌대학_A_지원_정보.getId(), differentTermUniversityInfoForApply.getId());
    }

    @Test
    void 좋아요_하지_않은_대학교에_좋아요를_누른다() {
        // request - 요청
        LikeResultResponse response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .log().all()
                .post("/universities/" + 괌대학_A_지원_정보.getId() + "/like")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(LikeResultResponse.class);

        Optional<LikedUniversity> likedUniversity
                = likedUniversityRepository.findAllBySiteUser_Id(siteUser.getId()).stream().findFirst();
        assertAll("좋아요 누른 대학교를 저장하고 좋아요 성공 응답을 반환한다.",
                () -> assertThat(likedUniversity).isPresent(),
                () -> assertThat(likedUniversity.get().getId()).isEqualTo(괌대학_A_지원_정보.getId()),
                () -> assertThat(response.result()).isEqualTo(LIKE_SUCCESS_MESSAGE)
        );
    }

    @Test
    void 대학의_좋아요_여부를_조회한다() {
        // setUp - 대학교 좋아요 저장
        likedUniversityRepository.save(createLikedUniversity(siteUser, 괌대학_A_지원_정보));

        // request - 요청
        IsLikeResponse response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .get("/universities/" + 괌대학_A_지원_정보.getId() + "/like")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(IsLikeResponse.class);

        assertThat(response.isLike()).isTrue();
    }
}
