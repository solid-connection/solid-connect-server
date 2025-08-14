package com.example.solidconnection.location.country.repository;

import com.example.solidconnection.location.country.domain.InterestedCountry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestedCountryRepository extends JpaRepository<InterestedCountry, Long> {

    List<InterestedCountry> findAllBySiteUserId(long siteUserId);

    void deleteBySiteUserId(long siteUserId);
}
