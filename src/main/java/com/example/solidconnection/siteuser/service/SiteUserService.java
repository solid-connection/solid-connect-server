package com.example.solidconnection.siteuser.service;

import com.example.solidconnection.siteuser.dto.NicknameExistsResponse;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SiteUserService {

    private final SiteUserRepository siteUserRepository;

    public NicknameExistsResponse checkNicknameExists(String nickname) {
        boolean exists = siteUserRepository.existsByNickname(nickname);
        return NicknameExistsResponse.from(exists);
    }
}
