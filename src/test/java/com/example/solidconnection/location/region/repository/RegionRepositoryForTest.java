package com.example.solidconnection.location.region.repository;

import com.example.solidconnection.location.region.domain.Region;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepositoryForTest extends JpaRepository<Region, Long> {

    Optional<Region> findByCode(String code);
}
