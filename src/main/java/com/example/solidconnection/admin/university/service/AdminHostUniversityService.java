package com.example.solidconnection.admin.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.COUNTRY_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.HOST_UNIVERSITY_ALREADY_EXISTS;
import static com.example.solidconnection.common.exception.ErrorCode.HOST_UNIVERSITY_HAS_REFERENCES;
import static com.example.solidconnection.common.exception.ErrorCode.REGION_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.UNIVERSITY_NOT_FOUND;

import com.example.solidconnection.admin.university.dto.AdminHostUniversityCreateRequest;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityDetailResponse;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityResponse;
import com.example.solidconnection.admin.university.dto.AdminHostUniversitySearchCondition;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityUpdateRequest;
import com.example.solidconnection.cache.annotation.DefaultCacheOut;
import com.example.solidconnection.cache.manager.CustomCacheManager;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.location.country.domain.Country;
import com.example.solidconnection.location.country.repository.CountryRepository;
import com.example.solidconnection.location.region.domain.Region;
import com.example.solidconnection.location.region.repository.RegionRepository;
import com.example.solidconnection.s3.domain.UploadDirectoryName;
import com.example.solidconnection.s3.domain.UploadPath;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.repository.HostUniversityRepository;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminHostUniversityService {

    private final HostUniversityRepository hostUniversityRepository;
    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;
    private final UnivApplyInfoRepository univApplyInfoRepository;
    private final CustomCacheManager cacheManager;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public Page<AdminHostUniversityResponse> getHostUniversities(
            AdminHostUniversitySearchCondition condition,
            Pageable pageable
    ) {
        Page<HostUniversity> hostUniversityPage = hostUniversityRepository.findAllBySearchCondition(
                condition.keyword(),
                condition.countryCode(),
                condition.regionCode(),
                pageable
        );
        return hostUniversityPage.map(AdminHostUniversityResponse::from);
    }

    @Transactional(readOnly = true)
    public AdminHostUniversityDetailResponse getHostUniversity(Long id) {
        HostUniversity hostUniversity = hostUniversityRepository.findById(id)
                .orElseThrow(() -> new CustomException(UNIVERSITY_NOT_FOUND));
        return AdminHostUniversityDetailResponse.from(hostUniversity);
    }

    @Transactional
    @DefaultCacheOut(
            key = {"univApplyInfoTextSearch", "university:recommend:general"},
            cacheManager = "customCacheManager",
            prefix = true
    )
    public AdminHostUniversityDetailResponse createHostUniversity(
            AdminHostUniversityCreateRequest request,
            MultipartFile logoFile,
            MultipartFile backgroundFile
    ) {
        validateKoreanNameNotExists(request.koreanName());

        Country country = findCountryByCode(request.countryCode());
        Region region = findRegionByCode(request.regionCode());
        String directoryName = UploadDirectoryName.fromUniversityNames(request.englishName(), request.koreanName());
        UploadedFileUrlResponse logoImage = null;
        UploadedFileUrlResponse backgroundImage = null;

        try {
            logoImage = uploadUniversityImage(
                    logoFile,
                    UploadPath.ADMIN_UNIVERSITY_LOGO,
                    directoryName
            );
            backgroundImage = uploadUniversityImage(
                    backgroundFile,
                    UploadPath.ADMIN_UNIVERSITY_BACKGROUND,
                    directoryName
            );

            HostUniversity hostUniversity = new HostUniversity(
                    null,
                    request.koreanName(),
                    request.englishName(),
                    request.formatName(),
                    request.homepageUrl(),
                    request.englishCourseUrl(),
                    request.accommodationUrl(),
                    logoImage.fileUrl(),
                    backgroundImage.fileUrl(),
                    request.detailsForLocal(),
                    country,
                    region
            );
            HostUniversity savedHostUniversity = hostUniversityRepository.saveAndFlush(hostUniversity);
            return AdminHostUniversityDetailResponse.from(savedHostUniversity);
        } catch (RuntimeException e) {
            deleteUploadedImages(logoImage, backgroundImage);
            throw e;
        }
    }

    private void validateKoreanNameNotExists(String koreanName) {
        hostUniversityRepository.findByKoreanName(koreanName)
                .ifPresent(existingUniversity -> {
                    throw new CustomException(HOST_UNIVERSITY_ALREADY_EXISTS);
                });
    }

    @Transactional
    @DefaultCacheOut(
            key = {"univApplyInfoTextSearch", "university:recommend:general"},
            cacheManager = "customCacheManager",
            prefix = true
    )
    public AdminHostUniversityDetailResponse updateHostUniversity(
            Long id,
            AdminHostUniversityUpdateRequest request,
            MultipartFile logoFile,
            MultipartFile backgroundFile
    ) {
        HostUniversity hostUniversity = hostUniversityRepository.findById(id)
                .orElseThrow(() -> new CustomException(UNIVERSITY_NOT_FOUND));

        validateKoreanNameNotDuplicated(request.koreanName(), id);

        Country country = findCountryByCode(request.countryCode());
        Region region = findRegionByCode(request.regionCode());
        String directoryName = UploadDirectoryName.fromUniversityNames(request.englishName(), request.koreanName());
        UploadedFileUrlResponse logoImage = null;
        UploadedFileUrlResponse backgroundImage = null;

        try {
            logoImage = uploadUniversityImageIfExists(
                    logoFile,
                    UploadPath.ADMIN_UNIVERSITY_LOGO,
                    directoryName
            );
            backgroundImage = uploadUniversityImageIfExists(
                    backgroundFile,
                    UploadPath.ADMIN_UNIVERSITY_BACKGROUND,
                    directoryName
            );

            hostUniversity.update(
                    request.koreanName(),
                    request.englishName(),
                    request.formatName(),
                    request.homepageUrl(),
                    request.englishCourseUrl(),
                    request.accommodationUrl(),
                    getImageUrlOrDefault(logoImage, hostUniversity.getLogoImageUrl()),
                    getImageUrlOrDefault(backgroundImage, hostUniversity.getBackgroundImageUrl()),
                    request.detailsForLocal(),
                    country,
                    region
            );
            hostUniversityRepository.flush();
            evictUnivApplyInfoDetailCaches(id);
            return AdminHostUniversityDetailResponse.from(hostUniversity);
        } catch (RuntimeException e) {
            deleteUploadedImages(logoImage, backgroundImage);
            throw e;
        }
    }

    private UploadedFileUrlResponse uploadUniversityImage(
            MultipartFile imageFile,
            UploadPath uploadPath,
            String directoryName
    ) {
        return s3Service.uploadFile(imageFile, uploadPath, directoryName);
    }

    private UploadedFileUrlResponse uploadUniversityImageIfExists(
            MultipartFile imageFile,
            UploadPath uploadPath,
            String directoryName
    ) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        return uploadUniversityImage(imageFile, uploadPath, directoryName);
    }

    private String getImageUrlOrDefault(UploadedFileUrlResponse uploadedImage, String defaultImageUrl) {
        if (uploadedImage == null) {
            return defaultImageUrl;
        }
        return uploadedImage.fileUrl();
    }

    private void deleteUploadedImages(UploadedFileUrlResponse... uploadedImages) {
        for (UploadedFileUrlResponse uploadedImage : uploadedImages) {
            if (uploadedImage != null) {
                try {
                    s3Service.deleteUploadedFile(uploadedImage);
                } catch (RuntimeException deleteException) {
                    log.warn(
                            "Failed to delete uploaded university image. fileUrl={}",
                            uploadedImage.fileUrl(),
                            deleteException
                    );
                }
            }
        }
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
    @DefaultCacheOut(
            key = {"univApplyInfoTextSearch", "university:recommend:general"},
            cacheManager = "customCacheManager",
            prefix = true
    )
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

    private void evictUnivApplyInfoDetailCaches(Long hostUniversityId) {
        List<Long> affectedUnivApplyInfoIds = univApplyInfoRepository.findIdsByUniversityId(hostUniversityId);

        List<String> cacheKeys = affectedUnivApplyInfoIds.stream()
                .map(univApplyInfoId -> "univApplyInfo:" + univApplyInfoId)
                .toList();

        cacheManager.evictMultiple(cacheKeys);
    }
}
