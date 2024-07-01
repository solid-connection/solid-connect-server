package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.dto.SignUpResponse;
import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.config.token.TokenValidator;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.InterestedCountry;
import com.example.solidconnection.entity.InterestedRegion;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.repositories.InterestedCountyRepository;
import com.example.solidconnection.repositories.InterestedRegionRepository;
import com.example.solidconnection.repositories.RegionRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_BIRTH_FORMAT;
import static com.example.solidconnection.custom.exception.ErrorCode.NICKNAME_ALREADY_EXISTED;
import static com.example.solidconnection.custom.exception.ErrorCode.USER_ALREADY_EXISTED;

@RequiredArgsConstructor
@Service
public class SignUpService {

    private final TokenValidator tokenValidator;
    private final TokenService tokenService;
    private final SiteUserRepository siteUserRepository;
    private final RegionRepository regionRepository;
    private final InterestedRegionRepository interestedRegionRepository;
    private final CountryRepository countryRepository;
    private final InterestedCountyRepository interestedCountyRepository;

    /*
    * 회원가입을 한다.
    * - 카카오로 최초 로그인 시 우리 서비스에서 발급한 카카오 토큰을 검증한다.
    *   - 이는 '카카오 인증을 하지 않고 회원가입 api 만으로 회원가입 하는 상황'을 방지하기 위함이다.
    *   - 만약 api 만으로 회원가입을 한다면, 이메일에 대한 검증 없이 회원가입이 가능해진다.
    *   - 이메일은 우리 서비스에서 사용자를 식별하는 중요한 정보이기 때문에 '우리 서비스에서 발급한 카카오 토큰 검증'단계가 필요하다.
    * - 관심 국가와 지역을 DB에 저장한다.
    *   - 관심 국가와 지역은 site_user_id를 참조하므로, 사용자 저장 후 저장한다.
    * */
    @Transactional
    public SignUpResponse signUp(SignUpRequest signUpRequest) {
        tokenValidator.validateKakaoToken(signUpRequest.kakaoOauthToken());
        String email = tokenService.getEmail(signUpRequest.kakaoOauthToken());
        validateUserNotDuplicated(email);
        validateNicknameDuplicated(signUpRequest.nickname());
        // validateBirthFormat(signUpRequest.birth());

        SiteUser siteUser = signUpRequest.toSiteUser(email, Role.MENTEE);
        SiteUser savedSiteUser = siteUserRepository.save(siteUser);

        saveInterestedRegion(signUpRequest, savedSiteUser);
        saveInterestedCountry(signUpRequest, savedSiteUser);

        String accessToken = tokenService.generateToken(email, TokenType.ACCESS);
        String refreshToken = tokenService.generateToken(email, TokenType.REFRESH);
        tokenService.saveToken(refreshToken, TokenType.REFRESH);

        return new SignUpResponse(accessToken, refreshToken);
    }

    private void validateUserNotDuplicated(String email){
        if(siteUserRepository.existsByEmail(email)){
            throw new CustomException(USER_ALREADY_EXISTED);
        }
    }

    private void validateNicknameDuplicated(String nickname){
        if(siteUserRepository.existsByNickname(nickname)){
            throw new CustomException(NICKNAME_ALREADY_EXISTED);
        }
    }

    private void validateBirthFormat(String birthInput) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate.parse(birthInput, formatter);
        } catch (DateTimeParseException e) {
            throw new CustomException(INVALID_BIRTH_FORMAT);
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
