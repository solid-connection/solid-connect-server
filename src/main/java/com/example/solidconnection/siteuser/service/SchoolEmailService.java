package com.example.solidconnection.siteuser.service;

import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_ALREADY_VERIFIED;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_ALREADY_USED;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_CONFIRM_CODE_DIFFERENT;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_CONFIRM_REQUEST_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_DOMAIN_NOT_SUPPORTED;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_VERIFICATION_INFO_CORRUPTED;
import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_VERIFICATION_INFO_SAVE_FAILED;
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
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

    @Transactional(readOnly = true)
    public void requestSchoolEmailVerification(long siteUserId, String schoolEmail) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (siteUser.getHomeUniversityId() != null) {
            throw new CustomException(SCHOOL_EMAIL_ALREADY_VERIFIED);
        }

        String normalizedSchoolEmail = normalizeSchoolEmail(schoolEmail);
        String domain = extractEmailDomain(normalizedSchoolEmail);
        HomeUniversity homeUniversity = homeUniversityRepository.findByEmailDomain(domain)
                .orElseThrow(() -> new CustomException(SCHOOL_EMAIL_DOMAIN_NOT_SUPPORTED));
        validateVerifiedSchoolEmailNotDuplicated(normalizedSchoolEmail);

        String code = generateVerificationCode();
        saveVerificationInfo(siteUserId, new SchoolVerificationInfo(normalizedSchoolEmail, homeUniversity.getId(), code));

        try {
            mailService.sendVerificationEmail(normalizedSchoolEmail, code);
        } catch (Exception e) {
            redisTemplate.delete(KEY_PREFIX + siteUserId);
            throw e;
        }
    }

    @Transactional
    public void confirmSchoolEmail(long siteUserId, String code) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        SchoolVerificationInfo info = getVerificationInfo(siteUserId);

        if (!info.getCode().equals(code)) {
            throw new CustomException(SCHOOL_EMAIL_CONFIRM_CODE_DIFFERENT);
        }

        String verifiedSchoolEmail = normalizeSchoolEmail(info.getSchoolEmail());
        validateVerifiedSchoolEmailNotDuplicated(verifiedSchoolEmail);
        siteUser.verifySchool(info.getHomeUniversityId(), verifiedSchoolEmail);
        flushVerifiedSchoolEmail();
        redisTemplate.delete(KEY_PREFIX + siteUserId);
    }

    private void flushVerifiedSchoolEmail() {
        try {
            siteUserRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(SCHOOL_EMAIL_ALREADY_USED);
        }
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
            SchoolVerificationInfo info = objectMapper.readValue(jsonInfo, SchoolVerificationInfo.class);
            validateVerificationInfo(siteUserId, info);
            return info;
        } catch (JsonProcessingException e) {
            throw corruptedVerificationInfoException(siteUserId);
        }
    }

    private void validateVerificationInfo(long siteUserId, SchoolVerificationInfo info) {
        if (info == null
                || isBlank(info.getSchoolEmail())
                || !hasEmailDomain(info.getSchoolEmail())
                || info.getHomeUniversityId() == null
                || isBlank(info.getCode())
                || !homeUniversityRepository.existsById(info.getHomeUniversityId())) {
            throw corruptedVerificationInfoException(siteUserId);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private CustomException corruptedVerificationInfoException(long siteUserId) {
        redisTemplate.delete(KEY_PREFIX + siteUserId);
        return new CustomException(SCHOOL_EMAIL_VERIFICATION_INFO_CORRUPTED);
    }

    private void validateVerifiedSchoolEmailNotDuplicated(String verifiedSchoolEmail) {
        if (siteUserRepository.existsByVerifiedSchoolEmail(verifiedSchoolEmail)) {
            throw new CustomException(SCHOOL_EMAIL_ALREADY_USED);
        }
    }

    private String normalizeSchoolEmail(String schoolEmail) {
        return schoolEmail.trim().toLowerCase(Locale.ROOT);
    }

    private String extractEmailDomain(String email) {
        if (!hasEmailDomain(email)) {
            throw new CustomException(SCHOOL_EMAIL_DOMAIN_NOT_SUPPORTED);
        }
        return email.substring(email.indexOf('@') + 1);
    }

    private boolean hasEmailDomain(String email) {
        if (email == null) {
            return false;
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 && atIndex < email.length() - 1;
    }

    private String generateVerificationCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }
}
