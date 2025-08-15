package com.example.solidconnection.location.region.repository;

import com.example.solidconnection.location.region.domain.InterestedRegion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestedRegionRepository extends JpaRepository<InterestedRegion, Long> {

    List<InterestedRegion> findAllBySiteUserId(long siteUserId);

    void deleteAllBySiteUserId(long siteUserId);
}
