package com.example.solidconnection.custom.security.userdetails;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.example.solidconnection.custom.exception.ErrorCode.AUTHENTICATION_FAILED;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_TOKEN;

@Service
@RequiredArgsConstructor
public class SiteUserDetailsService implements UserDetailsService {

    private final SiteUserRepository siteUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        long siteUserId = getSiteUserId(username);
        SiteUser siteUser = getSiteUser(siteUserId);
        validateNotQuit(siteUser);

        return new SiteUserDetails(siteUser);
    }

    private long getSiteUserId(String username) {
        try {
            return Long.parseLong(username);
        } catch (NumberFormatException e) {
            throw new CustomException(INVALID_TOKEN, "인증 정보가 지정된 형식과 일치하지 않습니다.");
        }
    }

    private SiteUser getSiteUser(long siteUserId) {
        return siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(AUTHENTICATION_FAILED, "인증 정보에 해당하는 사용자를 찾을 수 없습니다."));
    }

    private void validateNotQuit(SiteUser siteUser) {
        if (siteUser.getQuitedAt() != null) {
            throw new CustomException(AUTHENTICATION_FAILED, "탈퇴한 사용자입니다.");
        }
    }
}
