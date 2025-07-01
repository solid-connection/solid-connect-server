package com.example.solidconnection.university.repository;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.LikedUnivApplyInfo;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikedUnivApplyInfoRepository extends JpaRepository<LikedUnivApplyInfo, Long> {

    List<LikedUnivApplyInfo> findAllBySiteUser_Id(long siteUserId);

    int countBySiteUser_Id(long siteUserId);

    Optional<LikedUnivApplyInfo> findBySiteUserAndUnivApplyInfo(SiteUser siteUser, UnivApplyInfo univApplyInfo);
}
