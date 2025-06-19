package com.example.solidconnection.location.region.repository;

import com.example.solidconnection.location.region.domain.InterestedRegion;
import com.example.solidconnection.location.region.domain.Region;
import com.example.solidconnection.location.region.fixture.RegionFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@TestContainerSpringBootTest
public class InterestedRegionRepositoryTest {

    @Autowired
    private InterestedRegionRepository interestedRegionRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private RegionFixture regionFixture;

    @Nested
    class 사용자와_지역은_복합_유니크_제약_조건을_가진다 {

        @Test
        void 같은_사용자가_같은_지역에_관심_표시를_하면_예외_응답을_반환한다() {
            // given
            SiteUser user = siteUserFixture.사용자();
            Region region = regionFixture.영미권();

            InterestedRegion firstInterest = new InterestedRegion(user, region);
            interestedRegionRepository.save(firstInterest);

            InterestedRegion secondInterest = new InterestedRegion(user, region);

            // when & then
            assertThatCode(() -> interestedRegionRepository.save(secondInterest))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void 다른_사용자가_같은_지역에_관심_표시를_하면_정상_저장된다() {
            // given
            SiteUser user1 = siteUserFixture.사용자(1, "user1");
            SiteUser user2 = siteUserFixture.사용자(2, "user2");
            Region region = regionFixture.영미권();

            InterestedRegion firstInterest = new InterestedRegion(user1, region);
            interestedRegionRepository.save(firstInterest);

            InterestedRegion secondInterest = new InterestedRegion(user2, region);

            // when & then
            assertThatCode(() -> {
                InterestedRegion saved = interestedRegionRepository.save(secondInterest);
                assertThat(saved.getId()).isNotNull();
            }).doesNotThrowAnyException();
        }

        @Test
        void 같은_사용자가_다른_지역에_관심_표시를_하면_정상_저장된다() {
            // given
            SiteUser user = siteUserFixture.사용자();
            Region region1 = regionFixture.영미권();
            Region region2 = regionFixture.유럽();

            InterestedRegion firstInterest = new InterestedRegion(user, region1);
            interestedRegionRepository.save(firstInterest);

            InterestedRegion secondInterest = new InterestedRegion(user, region2);

            // when & then
            assertThatCode(() -> {
                InterestedRegion saved = interestedRegionRepository.save(secondInterest);
                assertThat(saved.getId()).isNotNull();
            }).doesNotThrowAnyException();
        }
    }
}
