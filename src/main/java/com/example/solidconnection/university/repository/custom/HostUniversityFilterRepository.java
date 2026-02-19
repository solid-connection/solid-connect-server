package com.example.solidconnection.university.repository.custom;

import com.example.solidconnection.university.domain.HostUniversity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HostUniversityFilterRepository {

    Page<HostUniversity> findAllBySearchCondition(
            String keyword,
            String countryCode,
            String regionCode,
            Pageable pageable
    );
}
