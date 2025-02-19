package com.example.solidconnection.university.repository;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.repository.custom.UniversityFilterRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.UNIVERSITY_NOT_FOUND;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long>, UniversityFilterRepository {

    @Query("SELECT u FROM University u WHERE u.country.code IN :countryCodes OR u.region.code IN :regionCodes")
    List<University> findByCountryCodeInOrRegionCodeIn(@Param("countryCodes") List<String> countryCodes, @Param("regionCodes") List<String> regionCodes);

    default University getUniversityById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(UNIVERSITY_NOT_FOUND));
    }
}
