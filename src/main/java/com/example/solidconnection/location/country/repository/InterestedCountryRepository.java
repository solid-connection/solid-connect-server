package com.example.solidconnection.location.country.repository;

import com.example.solidconnection.location.country.domain.InterestedCountry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface InterestedCountryRepository extends JpaRepository<InterestedCountry, Long> {

    List<InterestedCountry> findAllBySiteUserId(long siteUserId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteBySiteUserId(long siteUserId);
}
