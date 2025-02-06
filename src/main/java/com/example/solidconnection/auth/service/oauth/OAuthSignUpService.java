package com.example.solidconnection.auth.service.oauth;

import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.service.SignInService;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.InterestedCountry;
import com.example.solidconnection.entity.InterestedRegion;
import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.repositories.InterestedCountyRepository;
import com.example.solidconnection.repositories.InterestedRegionRepository;
import com.example.solidconnection.repositories.RegionRepository;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.NICKNAME_ALREADY_EXISTED;
import static com.example.solidconnection.custom.exception.ErrorCode.USER_ALREADY_EXISTED;

@RequiredArgsConstructor
@Service
public class OAuthSignUpService {

    private final SignUpTokenProvider signUpTokenProvider;
    private final SignInService signInService;
    private final SiteUserRepository siteUserRepository;
    private final RegionRepository regionRepository;
    private final InterestedRegionRepository interestedRegionRepository;
    private final CountryRepository countryRepository;
    private final InterestedCountyRepository interestedCountyRepository;

    /*
     * OAuth 인증 후 회원가입을 한다.
     * - 우리 서버에서 OAuth 인증했음을 확인하기 위한 signUpToken 을 검증한다.
     * - 사용자 정보를 DB에 저장한다.
     * - 관심 국가와 지역을 DB에 저장한다.
     *   - 관심 국가와 지역은 site_user_id를 참조하므로, 사용자 저장 후 저장한다.
     * - 바로 로그인하도록 액세스 토큰과 리프레시 토큰을 발급한다.
     * */
    @Transactional
    public SignInResponse signUp(SignUpRequest signUpRequest) {
        // 검증
        signUpTokenProvider.validateSignUpToken(signUpRequest.signUpToken());
        validateNicknameDuplicated(signUpRequest.nickname());
        String email = signUpTokenProvider.parseEmail(signUpRequest.signUpToken());
        AuthType authType = signUpTokenProvider.parseAuthType(signUpRequest.signUpToken());
        validateUserNotDuplicated(email, authType);

        // 사용자 저장
        SiteUser siteUser = siteUserRepository.save(signUpRequest.toSiteUser(email, authType));

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

    private void validateUserNotDuplicated(String email, AuthType authType) {
        if (siteUserRepository.existsByEmailAndAuthType(email, authType)) {
            throw new CustomException(USER_ALREADY_EXISTED);
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
}
