package com.example.solidconnection.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.SCHOOL_EMAIL_NOT_VERIFIED;
import static com.example.solidconnection.common.exception.ErrorCode.UNIVERSITY_NOT_FOUND;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.repository.HomeUniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeUniversityQueryService {

    private final HomeUniversityRepository homeUniversityRepository;

    @Transactional(readOnly = true)
    public String findNameByNullableId(Long homeUniversityId) {
        if (homeUniversityId == null) {
            return null;
        }

        return findNameById(homeUniversityId);
    }

    @Transactional(readOnly = true)
    public String findNameByRequiredId(Long homeUniversityId) {
        if (homeUniversityId == null) {
            throw new CustomException(SCHOOL_EMAIL_NOT_VERIFIED);
        }

        return findNameById(homeUniversityId);
    }

    private String findNameById(Long homeUniversityId) {
        HomeUniversity homeUniversity = homeUniversityRepository.findById(homeUniversityId)
                .orElseThrow(() -> new CustomException(UNIVERSITY_NOT_FOUND));
        return homeUniversity.getName();
    }
}
