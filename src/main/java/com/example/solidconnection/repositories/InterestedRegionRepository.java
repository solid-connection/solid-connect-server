package com.example.solidconnection.repositories;

import com.example.solidconnection.entity.InterestedRegion;
import com.example.solidconnection.siteuser.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestedRegionRepository extends JpaRepository<InterestedRegion, Long> {
    List<InterestedRegion> findAllBySiteUser(SiteUser siteUser);
}
