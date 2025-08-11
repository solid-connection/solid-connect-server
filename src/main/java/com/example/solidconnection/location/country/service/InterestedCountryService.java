package com.example.solidconnection.location.country.service;

import com.example.solidconnection.location.country.domain.InterestedCountry;
import com.example.solidconnection.location.country.repository.CountryRepository;
import com.example.solidconnection.location.country.repository.InterestedCountryRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterestedCountryService {

    private final CountryRepository countryRepository;
    private final InterestedCountryRepository interestedCountryRepository;

    public void saveInterestedCountry(SiteUser siteUser, List<String> koreanNames) {
        List<InterestedCountry> interestedCountries = countryRepository.findAllByKoreanNameIn(koreanNames)
                .stream()
                .map(country -> new InterestedCountry(siteUser, country))
                .toList();
        interestedCountryRepository.saveAll(interestedCountries);
    }
}
