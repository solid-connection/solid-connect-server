package com.example.solidconnection.location.country.repository;

import com.example.solidconnection.location.country.domain.InterestedCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestedCountryRepository extends JpaRepository<InterestedCountry, Long> {

    List<InterestedCountry> findAllBySiteUserId(long siteUserId);
}
