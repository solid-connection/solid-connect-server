package com.example.solidconnection.location.region.service;

import com.example.solidconnection.location.region.domain.InterestedRegion;
import com.example.solidconnection.location.region.repository.InterestedRegionRepository;
import com.example.solidconnection.location.region.repository.RegionRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterestedRegionService {

    private final RegionRepository regionRepository;
    private final InterestedRegionRepository interestedRegionRepository;

    @Transactional
    public void saveInterestedRegion(SiteUser siteUser, List<String> koreanNames) {
        List<InterestedRegion> interestedRegions = regionRepository.findAllByKoreanNameIn(koreanNames)
                .stream()
                .map(region -> new InterestedRegion(siteUser, region))
                .toList();
        interestedRegionRepository.saveAll(interestedRegions);
    }

    @Transactional
    public void updateInterestedRegion(SiteUser siteUser, List<String> koreanNames) {
        interestedRegionRepository.deleteBySiteUserId(siteUser.getId());

        List<InterestedRegion> interestedRegions = regionRepository.findAllByKoreanNameIn(koreanNames)
                .stream()
                .map(region -> new InterestedRegion(siteUser, region))
                .toList();
        interestedRegionRepository.saveAll(interestedRegions);
    }
}
