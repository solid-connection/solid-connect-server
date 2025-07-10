package com.example.solidconnection.university.repository;

import com.example.solidconnection.university.domain.LikedUnivApplyInfo;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikedUnivApplyInfoRepository extends JpaRepository<LikedUnivApplyInfo, Long> {

    List<LikedUnivApplyInfo> findAllBySiteUserId(long siteUserId);

    int countBySiteUserId(long siteUserId);

    Optional<LikedUnivApplyInfo> findBySiteUserIdAndUnivApplyInfoId(long siteUserId, long univApplyInfoId);

    @Query("""
            SELECT u
            FROM UnivApplyInfo u
            JOIN LikedUnivApplyInfo l ON u.id = l.univApplyInfoId
            WHERE l.siteUserId = :siteUserId
            """)
    List<UnivApplyInfo> findUnivApplyInfosBySiteUserId(@Param("siteUserId") long siteUserId);

    boolean existsBySiteUserIdAndUnivApplyInfoId(long siteUserId, long univApplyInfoId);
}
