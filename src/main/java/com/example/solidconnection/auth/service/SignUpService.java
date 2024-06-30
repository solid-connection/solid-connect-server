package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.dto.SignUpResponse;
import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.config.token.TokenValidator;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.InterestedCountry;
import com.example.solidconnection.entity.InterestedRegion;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.repositories.InterestedCountyRepository;
import com.example.solidconnection.repositories.InterestedRegionRepository;
import com.example.solidconnection.repositories.RegionRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.CountryCode;
import com.example.solidconnection.type.RegionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

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

    public SignUpResponse signUp(SignUpRequest signUpRequest) {
        tokenValidator.validateKakaoToken(signUpRequest.kakaoOauthToken());
        validateUserNotDuplicated(signUpRequest);
        validateNicknameDuplicated(signUpRequest.nickname());
        validateBirthFormat(signUpRequest.birth());

        SiteUser siteUser = makeSiteUserEntity(signUpRequest);
        SiteUser savedSiteUser = siteUserRepository.save(siteUser);

        saveInterestedRegion(signUpRequest, savedSiteUser);
        saveInterestedCountry(signUpRequest, savedSiteUser);

        String email = savedSiteUser.getEmail();
        String accessToken = tokenService.generateToken(email, TokenType.ACCESS);
        String refreshToken = tokenService.generateToken(email, TokenType.REFRESH);
        tokenService.saveToken(refreshToken, TokenType.REFRESH);

        return new SignUpResponse(accessToken, refreshToken);
    }

    private void validateUserNotDuplicated(SignUpRequest signUpRequest){
        String email = tokenService.getEmail(signUpRequest.kakaoOauthToken());
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

    private SiteUser makeSiteUserEntity(SignUpRequest signUpRequest) {
        signUpRequest.interestedCountries();

        return /*new SiteUser(
                tokenService.getEmail(signUpRequestDto.getKakaoOauthToken()),
                signUpRequestDto.getNickname(),
                signUpRequestDto.getProfileImageUrl(),
                signUpRequestDto.getBirth(),
                signUpRequestDto.getPreparationStatus(),
                Role.MENTEE,
                signUpRequestDto.getGender(),
                null,
                null);*/ null;
    }

    private void saveInterestedCountry(SignUpRequest signUpRequest, SiteUser savedSiteUser) {
        List<InterestedCountry> interestedCountries = signUpRequest.interestedCountries().stream()
                .map(CountryCode::getCountryCodeByKoreanName)
                .map(countryCode -> {
                    Country country = countryRepository.findByCode(countryCode)
                            .orElseThrow(() -> new RuntimeException("Country Code enum이랑 table이랑 다름 : " + countryCode.name()));
                    return InterestedCountry.builder()
                            .siteUser(savedSiteUser)
                            .country(country)
                            .build();
                })
                .collect(Collectors.toList());
        interestedCountyRepository.saveAll(interestedCountries);
    }

    private void saveInterestedRegion(SignUpRequest signUpRequest, SiteUser savedSiteUser) {
        List<InterestedRegion> interestedRegions = signUpRequest.interestedRegions().stream()
                .map(RegionCode::getRegionCodeByKoreanName)
                .map(regionCode -> {
                    Region region = regionRepository.findByCode(regionCode)
                            .orElseThrow(() -> new RuntimeException("Region Code enum이랑 table이랑 다름 : " + regionCode.name()));
                    return InterestedRegion.builder()
                            .siteUser(savedSiteUser)
                            .region(region)
                            .build();
                })
                .collect(Collectors.toList());
        interestedRegionRepository.saveAll(interestedRegions);
    }
}
