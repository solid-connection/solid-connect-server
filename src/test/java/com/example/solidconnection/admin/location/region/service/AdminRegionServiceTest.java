package com.example.solidconnection.admin.location.region.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.example.solidconnection.admin.location.region.dto.AdminRegionCreateRequest;
import com.example.solidconnection.admin.location.region.dto.AdminRegionResponse;
import com.example.solidconnection.admin.location.region.dto.AdminRegionUpdateRequest;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.location.region.domain.Region;
import com.example.solidconnection.location.region.fixture.RegionFixture;
import com.example.solidconnection.location.region.repository.RegionRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("지역 관련 관리자 서비스 테스트")
class AdminRegionServiceTest {

    @Autowired
    private AdminRegionService adminRegionService;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private RegionFixture regionFixture;

    @Nested
    class 전체_지역_조회 {

        @Test
        void 지역이_없으면_빈_목록을_반환한다() {
            // when
            List<AdminRegionResponse> responses = adminRegionService.getAllRegions();

            // then
            assertThat(responses).isEqualTo(List.of());
        }

        @Test
        void 저장된_모든_지역을_조회한다() {
            // given
            Region region1 = regionFixture.영미권();
            Region region2 = regionFixture.유럽();
            Region region3 = regionFixture.아시아();

            // when
            List<AdminRegionResponse> responses = adminRegionService.getAllRegions();

            // then
            assertThat(responses)
                    .hasSize(3)
                    .extracting(AdminRegionResponse::code)
                    .containsExactlyInAnyOrder(
                            region1.getCode(),
                            region2.getCode(),
                            region3.getCode()
                    );
        }
    }

    @Nested
    class 지역_생성 {

        @Test
        void 유효한_정보로_지역을_생성하면_성공한다() {
            // given
            AdminRegionCreateRequest request = new AdminRegionCreateRequest("KR_SEOUL", "서울");

            // when
            AdminRegionResponse response = adminRegionService.createRegion(request);

            // then
            assertThat(response.code()).isEqualTo("KR_SEOUL");

            // 데이터베이스에 저장되었는지 확인
            Region savedRegion = regionRepository.findById(request.code()).orElseThrow();
            assertThat(savedRegion.getKoreanName()).isEqualTo(request.koreanName());
        }

        @Test
        void 이미_존재하는_코드로_지역을_생성하면_예외_응답을_반환한다() {
            // given
            regionFixture.영미권();

            AdminRegionCreateRequest request = new AdminRegionCreateRequest("AMERICAS", "새로운 영미권");

            // when & then
            assertThatCode(() -> adminRegionService.createRegion(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.REGION_ALREADY_EXISTS.getMessage());
        }

        @Test
        void 이미_존재하는_한글명으로_지역을_생성하면_예외_응답을_반환한다() {
            // given
            regionFixture.유럽();

            AdminRegionCreateRequest request = new AdminRegionCreateRequest("NEW_CODE", "유럽");

            // when & then
            assertThatCode(() -> adminRegionService.createRegion(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.REGION_ALREADY_EXISTS.getMessage());
        }
    }

    @Nested
    class 지역_수정 {

        @Test
        void 유효한_정보로_지역을_수정하면_성공한다() {
            // given
            Region region = regionFixture.영미권();

            AdminRegionUpdateRequest request = new AdminRegionUpdateRequest("미주");

            // when
            AdminRegionResponse response = adminRegionService.updateRegion(region.getCode(), request);

            // then
            assertThat(response.code()).isEqualTo(region.getCode());
            Region updatedRegion = regionRepository.findById(region.getCode()).orElseThrow();
            assertThat(updatedRegion.getKoreanName()).isEqualTo(request.koreanName());
        }

        @Test
        void 존재하지_않는_지역_코드로_수정하면_예외_응답을_반환한다() {
            // given
            AdminRegionUpdateRequest request = new AdminRegionUpdateRequest("부산");

            // when & then
            assertThatCode(() -> adminRegionService.updateRegion("NOT_EXIST", request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.REGION_NOT_FOUND.getMessage());
        }

        @Test
        void 다른_지역의_한글명으로_수정하면_예외_응답을_반환한다() {
            // given
            Region region1 = regionFixture.영미권();
            Region region2 = regionFixture.유럽();

            AdminRegionUpdateRequest request = new AdminRegionUpdateRequest(region2.getKoreanName());

            // when & then
            assertThatCode(() -> adminRegionService.updateRegion(region1.getCode(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.REGION_ALREADY_EXISTS.getMessage());
        }

        @Test
        void 같은_지역의_한글명으로_수정하면_성공한다() {
            // given
            Region region = regionFixture.아시아();

            AdminRegionUpdateRequest request = new AdminRegionUpdateRequest(region.getKoreanName());

            // when
            AdminRegionResponse response = adminRegionService.updateRegion(region.getCode(), request);

            // then
            assertThat(response.code()).isEqualTo(region.getCode());
        }
    }

    @Nested
    class 지역_삭제 {

        @Test
        void 존재하는_지역을_삭제하면_성공한다() {
            // given
            Region region = regionFixture.영미권();

            // when
            adminRegionService.deleteRegion(region.getCode());

            // then
            assertThat(regionRepository.findById(region.getCode())).isEmpty();
        }

        @Test
        void 존재하지_않는_지역을_삭제하면_예외_응답을_반환한다() {
            // when & then
            assertThatCode(() -> adminRegionService.deleteRegion("NOT_EXIST"))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.REGION_NOT_FOUND.getMessage());
        }
    }
}
