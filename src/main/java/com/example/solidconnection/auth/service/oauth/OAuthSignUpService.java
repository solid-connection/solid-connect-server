package com.example.solidconnection.auth.service.oauth;

import static com.example.solidconnection.common.exception.ErrorCode.USER_ALREADY_EXISTED;

import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.service.SignInService;
import com.example.solidconnection.auth.service.SignUpService;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.location.country.repository.CountryRepository;
import com.example.solidconnection.location.country.service.InterestedCountryService;
import com.example.solidconnection.location.region.repository.RegionRepository;
import com.example.solidconnection.location.region.service.InterestedRegionService;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import org.springframework.stereotype.Service;

@Service
public class OAuthSignUpService extends SignUpService {

    private final OAuthSignUpTokenProvider oAuthSignUpTokenProvider;

    OAuthSignUpService(SignInService signInService, SiteUserRepository siteUserRepository,
                       RegionRepository regionRepository, InterestedRegionService interestedRegionService,
                       CountryRepository countryRepository, InterestedCountryService interestedCountryService,
                       OAuthSignUpTokenProvider oAuthSignUpTokenProvider) {
        super(signInService, siteUserRepository, regionRepository, interestedRegionService, countryRepository, interestedCountryService);
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
