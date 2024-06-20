package com.example.solidconnection.auth.service;


import com.example.solidconnection.auth.dto.ReissueResponseDto;
import com.example.solidconnection.auth.dto.SignUpRequestDto;
import com.example.solidconnection.auth.dto.SignUpResponseDto;
import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.config.token.TokenValidator;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.*;
import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.repositories.InterestedCountyRepository;
import com.example.solidconnection.repositories.InterestedRegionRepository;
import com.example.solidconnection.repositories.RegionRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.CountryCode;
import com.example.solidconnection.type.RegionCode;
import com.example.solidconnection.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.solidconnection.custom.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenValidator tokenValidator;
    private final TokenService tokenService;
    private final SiteUserRepository siteUserRepository;
    private final RegionRepository regionRepository;
    private final InterestedRegionRepository interestedRegionRepository;
    private final CountryRepository countryRepository;
    private final InterestedCountyRepository interestedCountyRepository;

    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto) {
        tokenValidator.validateKakaoToken(signUpRequestDto.getKakaoOauthToken());
        validateUserNotDuplicated(signUpRequestDto);
        validateNicknameDuplicated(signUpRequestDto.getNickname());
        validateBirthFormat(signUpRequestDto.getBirth());

        SiteUser siteUser = makeSiteUserEntity(signUpRequestDto);
        SiteUser savedSiteUser = siteUserRepository.save(siteUser);

        saveInterestedRegion(signUpRequestDto, savedSiteUser);
        saveInterestedCountry(signUpRequestDto, savedSiteUser);

        String email = savedSiteUser.getEmail();
        String accessToken = tokenService.generateToken(email, TokenType.ACCESS);
        String refreshToken = tokenService.generateToken(email, TokenType.REFRESH);
        tokenService.saveToken(refreshToken, TokenType.REFRESH);

        return SignUpResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public boolean signOut(String email){
        redisTemplate.opsForValue().set(
                TokenType.REFRESH.getPrefix() + email,
                "signOut",
                TokenType.REFRESH.getExpireTime(),
                TimeUnit.MILLISECONDS
        );
        return true;
    }

    public boolean quit(String email){
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        siteUser.setQuitedAt(LocalDate.now().plusDays(1));
        return true;
    }

    public ReissueResponseDto reissue(String email) {
        // 리프레시 토큰 만료 확인
        String refreshTokenKey= TokenType.REFRESH.getPrefix() + email;
        String refreshToken = redisTemplate.opsForValue().get(refreshTokenKey);
        if (ObjectUtils.isEmpty(refreshToken)) {
            throw new CustomException(REFRESH_TOKEN_EXPIRED);
        }
        // 엑세스 토큰 재발급
        String newAccessToken = tokenService.generateToken(email, TokenType.ACCESS);
        return ReissueResponseDto.builder()
                .accessToken(newAccessToken)
                .build();
    }

    private void validateUserNotDuplicated(SignUpRequestDto signUpRequestDto){
        String email = tokenService.getEmail(signUpRequestDto.getKakaoOauthToken());
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

    private SiteUser makeSiteUserEntity(SignUpRequestDto signUpRequestDto) {
        return SiteUser.builder()
                .email(tokenService.getEmail(signUpRequestDto.getKakaoOauthToken()))
                .nickname(signUpRequestDto.getNickname())
                .preparationStage(signUpRequestDto.getPreparationStatus())
                .profileImageUrl(signUpRequestDto.getProfileImageUrl())
                .gender(signUpRequestDto.getGender())
                .birth(signUpRequestDto.getBirth())
                .role(Role.MENTEE)
                .build();
    }

    private void saveInterestedCountry(SignUpRequestDto signUpRequestDto, SiteUser savedSiteUser) {
        List<InterestedCountry> interestedCountries = signUpRequestDto.getInterestedCountries().stream()
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

    private void saveInterestedRegion(SignUpRequestDto signUpRequestDto, SiteUser savedSiteUser) {
        List<InterestedRegion> interestedRegions = signUpRequestDto.getInterestedRegions().stream()
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
