package com.example.solidconnection.university.repository;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.repository.custom.UnivApplyInfoFilterRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.solidconnection.common.exception.ErrorCode.UNIV_APPLY_INFO_NOT_FOUND;

@Repository
public interface UnivApplyInfoRepository extends JpaRepository<UnivApplyInfo, Long>, UnivApplyInfoFilterRepository {

    @Query("""
            SELECT uai
            FROM UnivApplyInfo uai
            JOIN University u ON uai.university = u
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
                  AND uai.term = :term
            """)
    List<UnivApplyInfo> findAllBySiteUsersInterestedCountryOrRegionAndTerm(@Param("siteUser") SiteUser siteUser, @Param("term") String term);

    @Query(value = """
                SELECT *
                FROM university_info_for_apply
                WHERE term = :term
                ORDER BY RAND()
                LIMIT :limitNum
            """, nativeQuery = true)
    List<UnivApplyInfo> findRandomByTerm(@Param("term") String term, @Param("limitNum") int limitNum);

    default UnivApplyInfo getUnivApplyInfoById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(UNIV_APPLY_INFO_NOT_FOUND));
    }

    @Query("""
            SELECT DISTINCT uai
            FROM UnivApplyInfo uai
            JOIN FETCH uai.university u
            JOIN FETCH u.country c
            JOIN FETCH u.region r
            WHERE uai.id IN :ids
            """)
    List<UnivApplyInfo> findAllByIds(@Param("ids") List<Long> ids);
}
