package com.example.solidconnection.location.country.service;

import com.example.solidconnection.location.country.domain.InterestedCountry;
import com.example.solidconnection.location.country.repository.CountryRepository;
import com.example.solidconnection.location.country.repository.InterestedCountryRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterestedCountryService {

    private final CountryRepository countryRepository;
    private final InterestedCountryRepository interestedCountryRepository;

    @Transactional
    public void saveInterestedCountry(SiteUser siteUser, List<String> koreanNames) {
        List<InterestedCountry> interestedCountries = countryRepository.findAllByKoreanNameIn(koreanNames)
                .stream()
                .map(country -> new InterestedCountry(siteUser, country))
                .toList();
        interestedCountryRepository.saveAll(interestedCountries);
    }

    @Transactional
    public void updateInterestedCountry(SiteUser siteUser, List<String> koreanNames) {
        interestedCountryRepository.deleteBySiteUserId(siteUser.getId());

        List<InterestedCountry> interestedCountries = countryRepository.findAllByKoreanNameIn(koreanNames)
                .stream()
                .map(country -> new InterestedCountry(siteUser, country))
                .toList();
        interestedCountryRepository.saveAll(interestedCountries);
    }
}
