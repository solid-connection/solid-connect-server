package com.example.solidconnection.support.fixture;

import com.example.solidconnection.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionRepositoryForTest extends JpaRepository<Region, Long> {

    Optional<Region> findByCode(String code);
}
