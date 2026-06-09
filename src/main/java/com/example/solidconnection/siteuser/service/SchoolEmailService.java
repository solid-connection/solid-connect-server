package com.example.solidconnection.siteuser.service;

import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_ALREADY_VERIFIED;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_VERIFICATION_INFO_CORRUPTED;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_VERIFICATION_INFO_SAVE_FAILED;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_CONFIRM_CODE_DIFFERENT;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_CONFIRM_REQUEST_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_DOMAIN_NOT_SUPPORTED;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.mail.MailService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.SchoolVerificationInfo;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.repository.HomeUniversityRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SchoolEmailService {

    private static final long VERIFICATION_CODE_TTL_SECONDS = 300;
    private static final String KEY_PREFIX = "school-email:";

    private final SiteUserRepository siteUserRepository;
    private final HomeUniversityRepository homeUniversityRepository;
    private final MailService mailService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void requestSchoolEmailVerification(long siteUserId, String schoolEmail) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (siteUser.getHomeUniversityId() != null) {
            throw new CustomException(SCHOOL_EMAIL_ALREADY_VERIFIED);
        }

        String domain = extractEmailDomain(schoolEmail);
        HomeUniversity homeUniversity = homeUniversityRepository.findByEmailDomain(domain)
                .orElseThrow(() -> new CustomException(SCHOOL_EMAIL_DOMAIN_NOT_SUPPORTED));

        String code = generateVerificationCode();
        saveVerificationInfo(siteUserId, new SchoolVerificationInfo(schoolEmail, homeUniversity.getId(), code));

        mailService.sendVerificationEmail(schoolEmail, code);
    }

    @Transactional
    public void confirmSchoolEmail(long siteUserId, String code) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        SchoolVerificationInfo info = getVerificationInfo(siteUserId);

        if (!info.getCode().equals(code)) {
            throw new CustomException(SCHOOL_EMAIL_CONFIRM_CODE_DIFFERENT);
        }

        siteUser.verifySchool(info.getHomeUniversityId());
        redisTemplate.delete(KEY_PREFIX + siteUserId);
    }

    private void saveVerificationInfo(long siteUserId, SchoolVerificationInfo info) {
        try {
            redisTemplate.opsForValue().set(
                    KEY_PREFIX + siteUserId,
                    objectMapper.writeValueAsString(info),
                    VERIFICATION_CODE_TTL_SECONDS,
                    TimeUnit.SECONDS
            );
        } catch (JsonProcessingException e) {
            throw new CustomException(SCHOOL_EMAIL_VERIFICATION_INFO_SAVE_FAILED);
        }
    }

    private SchoolVerificationInfo getVerificationInfo(long siteUserId) {
        String jsonInfo = redisTemplate.opsForValue().get(KEY_PREFIX + siteUserId);
        if (jsonInfo == null) {
            throw new CustomException(SCHOOL_EMAIL_CONFIRM_REQUEST_NOT_FOUND);
        }
        try {
            return objectMapper.readValue(jsonInfo, SchoolVerificationInfo.class);
        } catch (JsonProcessingException e) {
            redisTemplate.delete(KEY_PREFIX + siteUserId);
            throw new CustomException(SCHOOL_EMAIL_VERIFICATION_INFO_CORRUPTED);
        }
    }

    private String extractEmailDomain(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex == -1) {
            throw new CustomException(SCHOOL_EMAIL_DOMAIN_NOT_SUPPORTED);
        }
        return email.substring(atIndex + 1);
    }

    private String generateVerificationCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }
}
