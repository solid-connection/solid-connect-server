package com.example.solidconnection.university.repository;

import static com.example.solidconnection.common.exception.ErrorCode.UNIVERSITY_NOT_FOUND;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.repository.custom.HostUniversityFilterRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostUniversityRepository extends JpaRepository<HostUniversity, Long>, HostUniversityFilterRepository {

    default HostUniversity getHostUniversityById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(UNIVERSITY_NOT_FOUND));
    }

    Optional<HostUniversity> findByKoreanName(String koreanName);
}
