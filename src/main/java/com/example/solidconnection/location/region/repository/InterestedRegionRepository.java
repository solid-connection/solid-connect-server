package com.example.solidconnection.location.region.repository;

import com.example.solidconnection.location.region.domain.InterestedRegion;
import com.example.solidconnection.siteuser.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestedRegionRepository extends JpaRepository<InterestedRegion, Long> {

    List<InterestedRegion> findAllBySiteUser(SiteUser siteUser);
}
