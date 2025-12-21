package com.example.solidconnection.admin.location.region.service;

import com.example.solidconnection.admin.location.region.dto.AdminRegionCreateRequest;
import com.example.solidconnection.admin.location.region.dto.AdminRegionResponse;
import com.example.solidconnection.admin.location.region.dto.AdminRegionUpdateRequest;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.location.region.domain.Region;
import com.example.solidconnection.location.region.repository.RegionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRegionService {

    private final RegionRepository regionRepository;

    @Transactional(readOnly = true)
    public List<AdminRegionResponse> getAllRegions() {
        return regionRepository.findAll()
                .stream()
                .map(AdminRegionResponse::from)
                .toList();
    }

    @Transactional
    public AdminRegionResponse createRegion(AdminRegionCreateRequest request) {
        validateCodeNotExists(request.code());
        validateKoreanNameNotExists(request.koreanName());

        Region region = new Region(request.code(), request.koreanName());
        Region savedRegion = regionRepository.save(region);

        return AdminRegionResponse.from(savedRegion);
    }

    private void validateCodeNotExists(String code) {
        regionRepository.findById(code)
                .ifPresent(region -> {
                    throw new CustomException(ErrorCode.REGION_ALREADY_EXISTS);
                });
    }

    private void validateKoreanNameNotExists(String koreanName) {
        regionRepository.findByKoreanName(koreanName)
                .ifPresent(region -> {
                    throw new CustomException(ErrorCode.REGION_ALREADY_EXISTS);
                });
    }

    @Transactional
    public AdminRegionResponse updateRegion(String code, AdminRegionUpdateRequest request) {
        Region region = regionRepository.findById(code)
                .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND));

        validateKoreanNameNotDuplicated(request.koreanName(), code);

        region.updateKoreanName(request.koreanName());
        return AdminRegionResponse.from(region);
    }

    private void validateKoreanNameNotDuplicated(String koreanName, String excludeCode) {
        regionRepository.findByKoreanName(koreanName)
                .ifPresent(existingRegion -> {
                    if (!existingRegion.getCode().equals(excludeCode)) {
                        throw new CustomException(ErrorCode.REGION_ALREADY_EXISTS);
                    }
                });
    }

    @Transactional
    public void deleteRegion(String code) {
        Region region = regionRepository.findById(code)
                .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND));

        regionRepository.delete(region);
    }
}
