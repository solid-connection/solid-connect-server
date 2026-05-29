package com.example.solidconnection.admin.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.HOME_UNIVERSITY_ALREADY_EXISTS;
import static com.example.solidconnection.common.exception.ErrorCode.HOME_UNIVERSITY_HAS_REFERENCES;
import static com.example.solidconnection.common.exception.ErrorCode.HOME_UNIVERSITY_NOT_FOUND;

import com.example.solidconnection.admin.university.dto.AdminHomeUniversityCreateRequest;
import com.example.solidconnection.admin.university.dto.AdminHomeUniversityResponse;
import com.example.solidconnection.admin.university.dto.AdminHomeUniversityUpdateRequest;
import com.example.solidconnection.cache.annotation.DefaultCacheOut;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.repository.HomeUniversityRepository;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminHomeUniversityService {

    private final HomeUniversityRepository homeUniversityRepository;
    private final UnivApplyInfoRepository univApplyInfoRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional(readOnly = true)
    public List<AdminHomeUniversityResponse> getAllHomeUniversities() {
        return homeUniversityRepository.findAll().stream()
                .map(AdminHomeUniversityResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminHomeUniversityResponse getHomeUniversity(Long id) {
        HomeUniversity homeUniversity = homeUniversityRepository.findById(id)
                .orElseThrow(() -> new CustomException(HOME_UNIVERSITY_NOT_FOUND));
        return AdminHomeUniversityResponse.from(homeUniversity);
    }

    @Transactional
    @DefaultCacheOut(
            key = {"univApplyInfoTextSearch", "university:recommend:general"},
            cacheManager = "customCacheManager",
            prefix = true
    )
    public AdminHomeUniversityResponse createHomeUniversity(AdminHomeUniversityCreateRequest request) {
        validateNameNotExists(request.name());
        HomeUniversity homeUniversity = new HomeUniversity(null, request.name());
        return AdminHomeUniversityResponse.from(homeUniversityRepository.save(homeUniversity));
    }

    @Transactional
    @DefaultCacheOut(
            key = {"univApplyInfoTextSearch", "university:recommend:general"},
            cacheManager = "customCacheManager",
            prefix = true
    )
    public AdminHomeUniversityResponse updateHomeUniversity(Long id, AdminHomeUniversityUpdateRequest request) {
        HomeUniversity homeUniversity = homeUniversityRepository.findById(id)
                .orElseThrow(() -> new CustomException(HOME_UNIVERSITY_NOT_FOUND));
        validateNameNotDuplicated(request.name(), id);
        homeUniversity.update(request.name());
        return AdminHomeUniversityResponse.from(homeUniversity);
    }

    @Transactional
    @DefaultCacheOut(
            key = {"univApplyInfoTextSearch", "university:recommend:general"},
            cacheManager = "customCacheManager",
            prefix = true
    )
    public void deleteHomeUniversity(Long id) {
        HomeUniversity homeUniversity = homeUniversityRepository.findById(id)
                .orElseThrow(() -> new CustomException(HOME_UNIVERSITY_NOT_FOUND));
        validateNoReferences(id);
        homeUniversityRepository.delete(homeUniversity);
    }

    private void validateNameNotExists(String name) {
        homeUniversityRepository.findByName(name)
                .ifPresent(existing -> {
                    throw new CustomException(HOME_UNIVERSITY_ALREADY_EXISTS);
                });
    }

    private void validateNameNotDuplicated(String name, Long excludeId) {
        homeUniversityRepository.findByName(name)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(excludeId)) {
                        throw new CustomException(HOME_UNIVERSITY_ALREADY_EXISTS);
                    }
                });
    }

    private void validateNoReferences(Long id) {
        if (univApplyInfoRepository.existsByHomeUniversityId(id)
                || siteUserRepository.existsByHomeUniversityId(id)) {
            throw new CustomException(HOME_UNIVERSITY_HAS_REFERENCES);
        }
    }
}
