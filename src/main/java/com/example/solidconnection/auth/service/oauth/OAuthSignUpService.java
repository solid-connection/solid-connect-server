package com.example.solidconnection.auth.service.oauth;

import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.service.SignInService;
import com.example.solidconnection.auth.service.SignUpService;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.repositories.InterestedCountyRepository;
import com.example.solidconnection.repositories.InterestedRegionRepository;
import com.example.solidconnection.repositories.RegionRepository;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import org.springframework.stereotype.Service;

import static com.example.solidconnection.custom.exception.ErrorCode.USER_ALREADY_EXISTED;

@Service
public class OAuthSignUpService extends SignUpService {

    private final OAuthSignUpTokenProvider oAuthSignUpTokenProvider;

    OAuthSignUpService(SignInService signInService, SiteUserRepository siteUserRepository,
                       RegionRepository regionRepository, InterestedRegionRepository interestedRegionRepository,
                       CountryRepository countryRepository, InterestedCountyRepository interestedCountyRepository,
                       OAuthSignUpTokenProvider oAuthSignUpTokenProvider) {
        super(signInService, siteUserRepository, regionRepository, interestedRegionRepository, countryRepository, interestedCountyRepository);
        this.oAuthSignUpTokenProvider = oAuthSignUpTokenProvider;
    }

    @Override
    protected void validateSignUpToken(SignUpRequest signUpRequest) {
        oAuthSignUpTokenProvider.validateSignUpToken(signUpRequest.signUpToken());
    }

    @Override
    protected void validateUserNotDuplicated(SignUpRequest signUpRequest) {
        String email = oAuthSignUpTokenProvider.parseEmail(signUpRequest.signUpToken());
        AuthType authType = oAuthSignUpTokenProvider.parseAuthType(signUpRequest.signUpToken());
        if (siteUserRepository.existsByEmailAndAuthType(email, authType)) {
            throw new CustomException(USER_ALREADY_EXISTED);
        }
    }

    @Override
    protected SiteUser createSiteUser(SignUpRequest signUpRequest) {
        String email = oAuthSignUpTokenProvider.parseEmail(signUpRequest.signUpToken());
        AuthType authType = oAuthSignUpTokenProvider.parseAuthType(signUpRequest.signUpToken());
        return signUpRequest.toOAuthSiteUser(email, authType);
    }
}
