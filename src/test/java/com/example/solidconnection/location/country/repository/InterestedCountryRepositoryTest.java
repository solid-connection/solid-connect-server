package com.example.solidconnection.location.country.repository;

import com.example.solidconnection.location.country.domain.Country;
import com.example.solidconnection.location.country.domain.InterestedCountry;
import com.example.solidconnection.location.country.fixture.CountryFixture;
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
public class InterestedCountryRepositoryTest {

    @Autowired
    private InterestedCountryRepository interestedCountryRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private CountryFixture countryFixture;

    @Nested
    class 사용자와_나라는_복합_유니크_제약_조건을_가진다 {

        @Test
        void 같은_사용자가_같은_나라에_관심_표시를_하면_예외가_발생한다() {
            // given
            SiteUser user = siteUserFixture.사용자();
            Country country = countryFixture.미국();
            
            InterestedCountry firstInterest = new InterestedCountry(user, country);
            interestedCountryRepository.save(firstInterest);
            
            InterestedCountry secondInterest = new InterestedCountry(user, country);
            
            // when & then
            assertThatCode(() -> interestedCountryRepository.save(secondInterest))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void 다른_사용자가_같은_나라에_관심_표시를_하면_정상_저장된다() {
            // given
            SiteUser user1 = siteUserFixture.사용자(1, "user1");
            SiteUser user2 = siteUserFixture.사용자(2, "user2");
            Country country = countryFixture.미국();
            
            InterestedCountry firstInterest = new InterestedCountry(user1, country);
            interestedCountryRepository.save(firstInterest);
            
            InterestedCountry secondInterest = new InterestedCountry(user2, country);
            
            // when & then
            assertThatCode(() -> {
                InterestedCountry saved = interestedCountryRepository.save(secondInterest);
                assertThat(saved.getId()).isNotNull();
            }).doesNotThrowAnyException();
        }

        @Test
        void 같은_사용자가_다른_나라에_관심_표시를_하면_정상_저장된다() {
            // given
            SiteUser user = siteUserFixture.사용자();
            Country country1 = countryFixture.미국();
            Country country2 = countryFixture.일본();
            
            InterestedCountry firstInterest = new InterestedCountry(user, country1);
            interestedCountryRepository.save(firstInterest);
            
            InterestedCountry secondInterest = new InterestedCountry(user, country2);
            
            // when & then
            assertThatCode(() -> {
                InterestedCountry saved = interestedCountryRepository.save(secondInterest);
                assertThat(saved.getId()).isNotNull();
            }).doesNotThrowAnyException();
        }
    }
}
