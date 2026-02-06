package com.example.solidconnection.university.repository;

import com.example.solidconnection.university.domain.HostUniversity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostUniversityRepositoryForTest extends JpaRepository<HostUniversity, Long> {

    Optional<HostUniversity> findByKoreanName(String koreanName);
}
