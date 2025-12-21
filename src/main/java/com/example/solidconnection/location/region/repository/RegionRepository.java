package com.example.solidconnection.location.region.repository;

import com.example.solidconnection.location.region.domain.Region;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, String> {

    List<Region> findAllByKoreanNameIn(List<String> koreanNames);

    Optional<Region> findByKoreanName(String koreanName);
}
