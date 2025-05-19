package com.example.solidconnection.location.region.repository;

import com.example.solidconnection.location.region.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    @Query("SELECT r FROM Region r WHERE r.koreanName IN :names")
    List<Region> findByKoreanNames(@Param(value = "names") List<String> names);
}
