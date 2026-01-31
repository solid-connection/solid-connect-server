package com.example.solidconnection.admin.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.COUNTRY_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.HOST_UNIVERSITY_ALREADY_EXISTS;
import static com.example.solidconnection.common.exception.ErrorCode.HOST_UNIVERSITY_HAS_REFERENCES;
import static com.example.solidconnection.common.exception.ErrorCode.REGION_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.UNIVERSITY_NOT_FOUND;

import com.example.solidconnection.admin.university.dto.AdminHostUniversityCreateRequest;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityDetailResponse;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityListResponse;
import com.example.solidconnection.admin.university.dto.AdminHostUniversitySearchCondition;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityUpdateRequest;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.location.country.domain.Country;
import com.example.solidconnection.location.country.repository.CountryRepository;
import com.example.solidconnection.location.region.domain.Region;
import com.example.solidconnection.location.region.repository.RegionRepository;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.repository.HostUniversityRepository;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminHostUniversityService {

    private final HostUniversityRepository hostUniversityRepository;
    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;
    private final UnivApplyInfoRepository univApplyInfoRepository;

    @Transactional(readOnly = true)
    public AdminHostUniversityListResponse getHostUniversities(
            AdminHostUniversitySearchCondition condition,
            Pageable pageable
    ) {
        Page<HostUniversity> hostUniversityPage = hostUniversityRepository.findAllBySearchCondition(
                condition.keyword(),
                condition.countryCode(),
                condition.regionCode(),
                pageable
        );
        return AdminHostUniversityListResponse.from(hostUniversityPage);
    }

    @Transactional(readOnly = true)
    public AdminHostUniversityDetailResponse getHostUniversity(Long id) {
        HostUniversity hostUniversity = hostUniversityRepository.findById(id)
                .orElseThrow(() -> new CustomException(UNIVERSITY_NOT_FOUND));
        return AdminHostUniversityDetailResponse.from(hostUniversity);
    }

    @Transactional
    public AdminHostUniversityDetailResponse createHostUniversity(AdminHostUniversityCreateRequest request) {
        validateKoreanNameNotExists(request.koreanName());

        Country country = findCountryByCode(request.countryCode());
        Region region = findRegionByCode(request.regionCode());

        HostUniversity hostUniversity = new HostUniversity(
                null,
                request.koreanName(),
                request.englishName(),
                request.formatName(),
                request.homepageUrl(),
                request.englishCourseUrl(),
                request.accommodationUrl(),
                request.logoImageUrl(),
                request.backgroundImageUrl(),
                request.detailsForLocal(),
                country,
                region
        );

        HostUniversity savedHostUniversity = hostUniversityRepository.save(hostUniversity);
        return AdminHostUniversityDetailResponse.from(savedHostUniversity);
    }

    private void validateKoreanNameNotExists(String koreanName) {
        hostUniversityRepository.findByKoreanName(koreanName)
                .ifPresent(existingUniversity -> {
                    throw new CustomException(HOST_UNIVERSITY_ALREADY_EXISTS);
                });
    }

    @Transactional
    public AdminHostUniversityDetailResponse updateHostUniversity(Long id, AdminHostUniversityUpdateRequest request) {
        HostUniversity hostUniversity = hostUniversityRepository.findById(id)
                .orElseThrow(() -> new CustomException(UNIVERSITY_NOT_FOUND));

        validateKoreanNameNotDuplicated(request.koreanName(), id);

        Country country = findCountryByCode(request.countryCode());
        Region region = findRegionByCode(request.regionCode());

        hostUniversity.update(
                request.koreanName(),
                request.englishName(),
                request.formatName(),
                request.homepageUrl(),
                request.englishCourseUrl(),
                request.accommodationUrl(),
                request.logoImageUrl(),
                request.backgroundImageUrl(),
                request.detailsForLocal(),
                country,
                region
        );

        return AdminHostUniversityDetailResponse.from(hostUniversity);
    }

    private void validateKoreanNameNotDuplicated(String koreanName, Long excludeId) {
        hostUniversityRepository.findByKoreanName(koreanName)
                .ifPresent(existingUniversity -> {
                    if (!existingUniversity.getId().equals(excludeId)) {
                        throw new CustomException(HOST_UNIVERSITY_ALREADY_EXISTS);
                    }
                });
    }

    private Country findCountryByCode(String countryCode) {
        return countryRepository.findByCode(countryCode)
                .orElseThrow(() -> new CustomException(COUNTRY_NOT_FOUND));
    }

    private Region findRegionByCode(String regionCode) {
        return regionRepository.findById(regionCode)
                .orElseThrow(() -> new CustomException(REGION_NOT_FOUND));
    }

    @Transactional
    public void deleteHostUniversity(Long id) {
        HostUniversity hostUniversity = hostUniversityRepository.findById(id)
                .orElseThrow(() -> new CustomException(UNIVERSITY_NOT_FOUND));

        validateNoReferences(id);

        hostUniversityRepository.delete(hostUniversity);
    }

    private void validateNoReferences(Long hostUniversityId) {
        if (univApplyInfoRepository.existsByUniversityId(hostUniversityId)) {
            throw new CustomException(HOST_UNIVERSITY_HAS_REFERENCES);
        }
    }
}
