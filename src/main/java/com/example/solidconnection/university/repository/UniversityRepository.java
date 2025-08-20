package com.example.solidconnection.university.repository;

import static com.example.solidconnection.common.exception.ErrorCode.UNIVERSITY_NOT_FOUND;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.university.domain.University;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, Long> {

    default University getUniversityById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(UNIVERSITY_NOT_FOUND));
    }
}
