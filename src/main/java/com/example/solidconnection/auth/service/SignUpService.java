package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.InterestedCountry;
import com.example.solidconnection.entity.InterestedRegion;
import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.repositories.InterestedCountyRepository;
import com.example.solidconnection.repositories.InterestedRegionRepository;
import com.example.solidconnection.repositories.RegionRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.NICKNAME_ALREADY_EXISTED;

/*
 * 우리 서버에서 인증되었음을 확인하기 위한 signUpToken 을 검증한다.
 * - 사용자 정보를 DB에 저장한다.
 * - 관심 국가와 지역을 DB에 저장한다.
 *   - 관심 국가와 지역은 site_user_id를 참조하므로, 사용자 저장 후 저장한다.
 * - 바로 로그인하도록 액세스 토큰과 리프레시 토큰을 발급한다.
 * */
public abstract class SignUpService {

    protected final SignInService signInService;
    protected final SiteUserRepository siteUserRepository;
    protected final RegionRepository regionRepository;
    protected final InterestedRegionRepository interestedRegionRepository;
    protected final CountryRepository countryRepository;
    protected final InterestedCountyRepository interestedCountyRepository;

    protected SignUpService(SignInService signInService, SiteUserRepository siteUserRepository,
                            RegionRepository regionRepository, InterestedRegionRepository interestedRegionRepository,
                            CountryRepository countryRepository, InterestedCountyRepository interestedCountyRepository) {
        this.signInService = signInService;
        this.siteUserRepository = siteUserRepository;
        this.regionRepository = regionRepository;
        this.interestedRegionRepository = interestedRegionRepository;
        this.countryRepository = countryRepository;
        this.interestedCountyRepository = interestedCountyRepository;
    }

    @Transactional
    public SignInResponse signUp(SignUpRequest signUpRequest) {
        // 검증
        validateSignUpToken(signUpRequest);
        validateUserNotDuplicated(signUpRequest);
        validateNicknameDuplicated(signUpRequest.nickname());

        // 사용자 저장
        SiteUser siteUser = siteUserRepository.save(createSiteUser(signUpRequest));

        // 관심 지역, 국가 저장
        saveInterestedRegion(signUpRequest, siteUser);
        saveInterestedCountry(signUpRequest, siteUser);

        // 로그인
        return signInService.signIn(siteUser);
    }

    private void validateNicknameDuplicated(String nickname) {
        if (siteUserRepository.existsByNickname(nickname)) {
            throw new CustomException(NICKNAME_ALREADY_EXISTED);
        }
    }

    private void saveInterestedRegion(SignUpRequest signUpRequest, SiteUser savedSiteUser) {
        List<String> interestedRegionNames = signUpRequest.interestedRegions();
        List<InterestedRegion> interestedRegions = regionRepository.findByKoreanNames(interestedRegionNames).stream()
                .map(region -> new InterestedRegion(savedSiteUser, region))
                .toList();
        interestedRegionRepository.saveAll(interestedRegions);
    }

    private void saveInterestedCountry(SignUpRequest signUpRequest, SiteUser savedSiteUser) {
        List<String> interestedCountryNames = signUpRequest.interestedCountries();
        List<InterestedCountry> interestedCountries = countryRepository.findByKoreanNames(interestedCountryNames).stream()
                .map(country -> new InterestedCountry(savedSiteUser, country))
                .toList();
        interestedCountyRepository.saveAll(interestedCountries);
    }

    protected abstract void validateSignUpToken(SignUpRequest signUpRequest);
    protected abstract void validateUserNotDuplicated(SignUpRequest signUpRequest);
    protected abstract SiteUser createSiteUser(SignUpRequest signUpRequest);
}
