package com.example.solidconnection.admin.location.country.service;

import com.example.solidconnection.admin.location.country.dto.AdminCountryCreateRequest;
import com.example.solidconnection.admin.location.country.dto.AdminCountryResponse;
import com.example.solidconnection.admin.location.country.dto.AdminCountryUpdateRequest;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.location.country.domain.Country;
import com.example.solidconnection.location.country.repository.CountryRepository;
import com.example.solidconnection.location.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCountryService {

    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;

    @Transactional(readOnly = true)
    public List<AdminCountryResponse> getAllCountries() {
        return countryRepository.findAll()
                .stream()
                .map(AdminCountryResponse::from)
                .toList();
    }

    @Transactional
    public AdminCountryResponse createCountry(AdminCountryCreateRequest request) {
        validateCodeNotExists(request.code());
        validateKoreanNameNotExists(request.koreanName());
        validateRegionCodeExists(request.regionCode());

        Country country = new Country(request.code(), request.koreanName(), request.regionCode());
        Country savedCountry = countryRepository.save(country);

        return AdminCountryResponse.from(savedCountry);
    }

    private void validateCodeNotExists(String code) {
        countryRepository.findByCode(code)
                .ifPresent(country -> {
                    throw new CustomException(ErrorCode.COUNTRY_ALREADY_EXISTS);
                });
    }

    private void validateKoreanNameNotExists(String koreanName) {
        countryRepository.findAllByKoreanNameIn(List.of(koreanName))
                .stream()
                .findFirst()
                .ifPresent(country -> {
                    throw new CustomException(ErrorCode.COUNTRY_ALREADY_EXISTS);
                });
    }

    private void validateRegionCodeExists(String regionCode) {
        if (regionCode != null) {
            regionRepository.findById(regionCode)
                    .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND));
        }
    }

    @Transactional
    public AdminCountryResponse updateCountry(String code, AdminCountryUpdateRequest request) {
        Country country = countryRepository.findByCode(code)
                .orElseThrow(() -> new CustomException(ErrorCode.COUNTRY_NOT_FOUND));

        validateKoreanNameNotDuplicated(request.koreanName(), code);
        validateRegionCodeExists(request.regionCode());

        country.updateKoreanName(request.koreanName());
        country.updateRegionCode(request.regionCode());

        return AdminCountryResponse.from(country);
    }

    private void validateKoreanNameNotDuplicated(String koreanName, String excludeCode) {
        countryRepository.findAllByKoreanNameIn(List.of(koreanName))
                .stream()
                .findFirst()
                .ifPresent(existingCountry -> {
                    if (!existingCountry.getCode().equals(excludeCode)) {
                        throw new CustomException(ErrorCode.COUNTRY_ALREADY_EXISTS);
                    }
                });
    }

    @Transactional
    public void deleteCountry(String code) {
        Country country = countryRepository.findByCode(code)
                .orElseThrow(() -> new CustomException(ErrorCode.COUNTRY_NOT_FOUND));

        countryRepository.delete(country);
    }
}
