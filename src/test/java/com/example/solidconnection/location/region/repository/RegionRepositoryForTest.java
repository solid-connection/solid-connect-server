package com.example.solidconnection.location.region.repository;

import com.example.solidconnection.location.region.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepositoryForTest extends JpaRepository<Region, Long> {

    Optional<Region> findByCode(String code);
}
