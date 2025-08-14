package com.example.solidconnection.location.country.repository;

import com.example.solidconnection.location.country.domain.InterestedCountry;
import com.example.solidconnection.siteuser.domain.SiteUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface InterestedCountryRepository extends JpaRepository<InterestedCountry, Long> {

    List<InterestedCountry> findAllBySiteUserId(long siteUserId);

    @Modifying
    void deleteBySiteUser(SiteUser siteUser);
}
