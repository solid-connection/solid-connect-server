package com.example.solidconnection.university.repository;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.repository.custom.UnivApplyInfoFilterRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.solidconnection.common.exception.ErrorCode.UNIV_APPLY_INFO_NOT_FOUND;

@Repository
public interface UnivApplyInfoRepository extends JpaRepository<UnivApplyInfo, Long>, UnivApplyInfoFilterRepository {

    @Query("""
        SELECT DISTINCT uai
        FROM UnivApplyInfo uai
        LEFT JOIN FETCH uai.languageRequirements lr
        JOIN FETCH uai.university u
        LEFT JOIN FETCH u.country c
        LEFT JOIN FETCH u.region r
        WHERE (c.code IN (
                  SELECT ic.countryCode
                  FROM InterestedCountry ic
                  WHERE ic.siteUserId = :siteUserId
              )
              OR r.code IN (
                  SELECT ir.regionCode
                  FROM InterestedRegion ir
                  WHERE ir.siteUserId = :siteUserId
              ))
              AND uai.term = :term
    """)
    List<UnivApplyInfo> findAllBySiteUsersInterestedCountryOrRegionAndTerm(@Param("siteUserId") Long siteUserId, @Param("term") String term);

    @Query("""
    SELECT DISTINCT uai
    FROM UnivApplyInfo uai
    LEFT JOIN FETCH uai.languageRequirements lr
    LEFT JOIN FETCH uai.university u
    LEFT JOIN FETCH u.country c
    LEFT JOIN FETCH u.region r
    WHERE uai.term = :term
    ORDER BY FUNCTION('RAND')
    """)
    List<UnivApplyInfo> findAllRandomByTerm(@Param("term") String term);
    default List<UnivApplyInfo> findRandomByTerm(String term, int limitNum) {
        return findAllRandomByTerm(term).stream()
                .limit(limitNum)
                .collect(Collectors.toList());
    }

    default UnivApplyInfo getUnivApplyInfoById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(UNIV_APPLY_INFO_NOT_FOUND));
    }

    @Query("""
            SELECT DISTINCT uai
            FROM UnivApplyInfo uai
            LEFT JOIN FETCH uai.languageRequirements lr
            LEFT JOIN FETCH uai.university u
            LEFT JOIN FETCH u.country c
            LEFT JOIN FETCH u.region r
            WHERE uai.id IN :ids
            """)
    List<UnivApplyInfo> findAllByIds(@Param("ids") List<Long> ids);
}
