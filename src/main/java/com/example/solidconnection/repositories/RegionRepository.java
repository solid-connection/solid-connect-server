package com.example.solidconnection.repositories;

import com.example.solidconnection.entity.Region;
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
