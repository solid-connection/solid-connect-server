package com.example.solidconnection.location.region.repository;

import com.example.solidconnection.location.region.domain.Region;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegionRepository extends JpaRepository<Region, Long> {

    @Query("SELECT r FROM Region r WHERE r.koreanName IN :names")
    List<Region> findByKoreanNames(@Param(value = "names") List<String> names);

    Optional<Region> findByKoreanName(String koreanName);
}
