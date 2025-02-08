package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.SignUpRequest;
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
public class EmailSignUpService extends SignUpService {

    private final EmailSignUpTokenProvider emailSignUpTokenProvider;

    public EmailSignUpService(SignInService signInService, SiteUserRepository siteUserRepository,
                              RegionRepository regionRepository, InterestedRegionRepository interestedRegionRepository,
                              CountryRepository countryRepository, InterestedCountyRepository interestedCountyRepository,
                              EmailSignUpTokenProvider emailSignUpTokenProvider) {
        super(signInService, siteUserRepository, regionRepository, interestedRegionRepository, countryRepository, interestedCountyRepository);
        this.emailSignUpTokenProvider = emailSignUpTokenProvider;
    }

    public void validateUniqueEmail(String email) {
        if (siteUserRepository.existsByEmailAndAuthType(email, AuthType.EMAIL)) {
            throw new CustomException(USER_ALREADY_EXISTED);
        }
    }

    @Override
    protected void validateSignUpToken(SignUpRequest signUpRequest) {
        emailSignUpTokenProvider.validateSignUpToken(signUpRequest.signUpToken());
    }

    @Override
    protected void validateUserNotDuplicated(SignUpRequest signUpRequest) {
        String email = emailSignUpTokenProvider.parseEmail(signUpRequest.signUpToken());
        if (siteUserRepository.existsByEmailAndAuthType(email, AuthType.EMAIL)) {
            throw new CustomException(USER_ALREADY_EXISTED);
        }
    }

    @Override
    protected SiteUser createSiteUser(SignUpRequest signUpRequest) {
        String email = emailSignUpTokenProvider.parseEmail(signUpRequest.signUpToken());
        String encodedPassword = emailSignUpTokenProvider.parseEncodedPassword(signUpRequest.signUpToken());
        return signUpRequest.toEmailSiteUser(email, encodedPassword);
    }
}
