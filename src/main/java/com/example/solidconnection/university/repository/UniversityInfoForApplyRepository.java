package com.example.solidconnection.university.repository;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.domain.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.example.solidconnection.common.exception.ErrorCode.UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND;

@Repository
public interface UniversityInfoForApplyRepository extends JpaRepository<UnivApplyInfo, Long> {

    Optional<UnivApplyInfo> findByIdAndTerm(Long id, String term);

    Optional<UnivApplyInfo> findFirstByKoreanNameAndTerm(String koreanName, String term);

    @Query("""
            SELECT uifa 
            FROM UnivApplyInfo uifa 
            WHERE uifa.university IN :universities 
                AND uifa.term = :term
            """)
    List<UnivApplyInfo> findByUniversitiesAndTerm(@Param("universities") List<University> universities, @Param("term") String term);

    @Query("""
            SELECT uifa
            FROM UnivApplyInfo uifa
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
    List<UnivApplyInfo> findUniversityInfoForAppliesBySiteUsersInterestedCountryOrRegionAndTerm(@Param("siteUser") SiteUser siteUser, @Param("term") String term);

    @Query(value = """
                SELECT *
                FROM university_info_for_apply
                WHERE term = :term
                ORDER BY RAND() 
                LIMIT :limitNum
            """, nativeQuery = true)
    List<UnivApplyInfo> findRandomByTerm(@Param("term") String term, @Param("limitNum") int limitNum);

    default UnivApplyInfo getUniversityInfoForApplyById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND));
    }

    @Query("""
            SELECT DISTINCT uifa
            FROM UnivApplyInfo uifa
            JOIN FETCH uifa.university u
            JOIN FETCH u.country c
            JOIN FETCH u.region r
            WHERE uifa.id IN :ids
            """)
    List<UnivApplyInfo> findAllByUniversityIds(@Param("ids") List<Long> ids);

    @Query("""
            SELECT DISTINCT uifa
            FROM UnivApplyInfo uifa
            JOIN FETCH uifa.university u
            JOIN FETCH u.country c
            JOIN FETCH u.region r
            WHERE u.id IN :universityIds
            """)
    List<UnivApplyInfo> findByUniversityIdsWithUniversityAndLocation(@Param("universityIds") List<Long> universityIds);
}
