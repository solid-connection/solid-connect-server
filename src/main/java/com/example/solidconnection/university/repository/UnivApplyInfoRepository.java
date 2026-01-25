package com.example.solidconnection.university.repository;

import static com.example.solidconnection.common.exception.ErrorCode.UNIV_APPLY_INFO_NOT_FOUND;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.repository.custom.UnivApplyInfoFilterRepository;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UnivApplyInfoRepository extends JpaRepository<UnivApplyInfo, Long>, UnivApplyInfoFilterRepository {

    @Query("""
               SELECT DISTINCT uai
               FROM UnivApplyInfo uai
               LEFT JOIN FETCH uai.languageRequirements lr
               LEFT JOIN FETCH uai.homeUniversity hu
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
                     AND uai.termId = :termId
           """)
    List<UnivApplyInfo> findAllBySiteUsersInterestedCountryOrRegionAndTermId(@Param("siteUserId") Long siteUserId, @Param("termId") long termId);

    @Query("""
               SELECT uai
               FROM UnivApplyInfo uai
               LEFT JOIN FETCH uai.languageRequirements lr
               LEFT JOIN FETCH uai.homeUniversity hu
               LEFT JOIN FETCH uai.university u
               LEFT JOIN FETCH u.country c
               LEFT JOIN FETCH u.region r
               WHERE uai.termId = :termId
               ORDER BY FUNCTION('RAND')
           """)
    List<UnivApplyInfo> findRandomByTermId(@Param("termId") long termId, Pageable pageable); // JPA에서 LIMIT 사용이 불가하므로 Pageable을 통해 0page에서 정해진 개수 만큼 가져오는 방식으로 구현

    default UnivApplyInfo getUnivApplyInfoById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(UNIV_APPLY_INFO_NOT_FOUND));
    }

    @Query("""
           SELECT DISTINCT uai
           FROM UnivApplyInfo uai
           LEFT JOIN FETCH uai.languageRequirements lr
           LEFT JOIN FETCH uai.homeUniversity hu
           LEFT JOIN FETCH uai.university u
           LEFT JOIN FETCH u.country c
           LEFT JOIN FETCH u.region r
           WHERE uai.id IN :ids
           """)
    List<UnivApplyInfo> findAllByIds(@Param("ids") List<Long> ids);
}
