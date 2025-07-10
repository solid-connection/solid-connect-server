package com.example.solidconnection.location.country.repository;

import com.example.solidconnection.location.country.domain.InterestedCountry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestedCountryRepository extends JpaRepository<InterestedCountry, Long> {

    List<InterestedCountry> findAllBySiteUserId(long siteUserId);
}
