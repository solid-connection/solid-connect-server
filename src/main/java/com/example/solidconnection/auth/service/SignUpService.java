package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.dto.SignUpResponse;
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
import com.example.solidconnection.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.NICKNAME_ALREADY_EXISTED;
import static com.example.solidconnection.custom.exception.ErrorCode.USER_ALREADY_EXISTED;

@RequiredArgsConstructor
@Service
public class SignUpService {

    private final TokenValidator tokenValidator;
    private final AuthTokenProvider authTokenProvider;
    private final SiteUserRepository siteUserRepository;
    private final RegionRepository regionRepository;
    private final InterestedRegionRepository interestedRegionRepository;
    private final CountryRepository countryRepository;
    private final InterestedCountyRepository interestedCountyRepository;

    /*
     * 회원가입을 한다.
     * - 카카오로 최초 로그인 시 우리 서비스에서 발급한 카카오 토큰 signUpToken 을 검증한다.
     *   - 이는 '카카오 인증을 하지 않고 회원가입 api 만으로 회원가입 하는 상황'을 방지하기 위함이다.
     *   - 만약 api 만으로 회원가입을 한다면, 카카오 인증과 이메일에 대한 검증 없이 회원가입이 가능해진다.
     *   - 이메일은 우리 서비스에서 사용자를 식별하는 중요한 정보이기 때문에 '우리 서비스에서 발급한 카카오 토큰인지 검증하는' 단계가 필요하다.
     * - 사용자 정보를 DB에 저장한다.
     * - 관심 국가와 지역을 DB에 저장한다.
     *   - 관심 국가와 지역은 site_user_id를 참조하므로, 사용자 저장 후 저장한다.
     * - 바로 로그인하도록 액세스 토큰과 리프레시 토큰을 발급한다.
     * */
    // todo: 여러가지 가입 방법 적용해야 함
    @Transactional
    public SignUpResponse signUp(SignUpRequest signUpRequest) {
        // 검증
        tokenValidator.validateKakaoToken(signUpRequest.signUpToken());
        String email = authTokenProvider.getEmail(signUpRequest.signUpToken());
        validateNicknameDuplicated(signUpRequest.nickname());
        validateUserNotDuplicated(email);

        // 사용자 저장
        SiteUser siteUser = signUpRequest.toSiteUser(email, Role.MENTEE);
        SiteUser savedSiteUser = siteUserRepository.save(siteUser);

        // 관심 지역, 국가 저장
        saveInterestedRegion(signUpRequest, savedSiteUser);
        saveInterestedCountry(signUpRequest, savedSiteUser);

        // 토큰 발급
        String accessToken = authTokenProvider.generateAccessToken(siteUser);
        String refreshToken = authTokenProvider.generateAndSaveRefreshToken(siteUser);
        return new SignUpResponse(accessToken, refreshToken);
    }

    private void validateUserNotDuplicated(String email) {
        if (siteUserRepository.existsByEmailAndAuthType(email, AuthType.KAKAO)) {
            throw new CustomException(USER_ALREADY_EXISTED);
        }
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
}
