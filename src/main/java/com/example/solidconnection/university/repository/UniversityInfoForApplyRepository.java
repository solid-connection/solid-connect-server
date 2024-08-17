package com.example.solidconnection.university.repository;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND;
import static com.example.solidconnection.custom.exception.ErrorCode.UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND_FOR_TERM;

@Repository
public interface UniversityInfoForApplyRepository extends JpaRepository<UniversityInfoForApply, Long> {

    Optional<UniversityInfoForApply> findByIdAndTerm(Long id, String term);

    Optional<UniversityInfoForApply> findByKoreanNameAndTerm(String koreanName, String term);

    @Query("SELECT c FROM UniversityInfoForApply c WHERE c.university IN :universities AND c.term = :term")
    List<UniversityInfoForApply> findByUniversitiesAndTerm(@Param("universities") List<University> universities, @Param("term") String term);

    @Query("""
            SELECT uifa
            FROM UniversityInfoForApply uifa
            JOIN University u ON uifa.university = u
            WHERE (u.country.code IN (
                      SELECT c.code
                      FROM InterestedCountry ic
                      JOIN ic.country c
                      WHERE ic.siteUser = :siteUser
                  )
                  OR u.region.code IN (
                      SELECT r.code
                      FROM InterestedRegion ir
                      JOIN ir.region r
                      WHERE ir.siteUser = :siteUser
                  ))
                  AND uifa.term = :term
            """)
    List<UniversityInfoForApply> findUniversityInfoForAppliesBySiteUsersInterestedCountryOrRegionAndTerm(@Param("siteUser") SiteUser siteUser, @Param("term") String term);

    default UniversityInfoForApply getUniversityInfoForApplyById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND));
    }

    default UniversityInfoForApply getUniversityInfoForApplyByIdAndTerm(Long id, String term) {
        return findByIdAndTerm(id, term)
                .orElseThrow(() -> new CustomException(UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND_FOR_TERM));
    }
}
