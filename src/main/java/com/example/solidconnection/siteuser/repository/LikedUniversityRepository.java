package com.example.solidconnection.siteuser.repository;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.LikedUniversity;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikedUniversityRepository extends JpaRepository<LikedUniversity, Long> {

    List<LikedUniversity> findAllBySiteUserId(long siteUserId);

    int countBySiteUserId(long siteUserId);

    Optional<LikedUniversity> findBySiteUserIdAndUnivApplyInfoId(long siteUserId, long univApplyInfoId);
    @Query("""
            SELECT u
            FROM UnivApplyInfo u
            JOIN LikedUniversity l ON u.id = l.univApplyInfoId
            WHERE l.siteUserId = :siteUserId
            """)
    List<UnivApplyInfo> findUnivApplyInfosBySiteUserId(@Param("siteUserId") Long siteUserId);

    boolean existsBySiteUserIdAndUnivApplyInfoId(Long siteUserId, Long univApplyInfoId);
}
