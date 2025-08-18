package com.example.solidconnection.location.country.repository;

import com.example.solidconnection.location.country.domain.Country;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CountryRepository extends JpaRepository<Country, Long> {

    List<Country> findAllByKoreanNameIn(List<String> koreanNames);

    @Query("""
           SELECT DISTINCT c.koreanName
           FROM Country c
           WHERE c.code IN (
               SELECT ic.countryCode
               FROM InterestedCountry ic
               WHERE ic.siteUserId = :siteUserId
           )
           """)
    List<String> findKoreanNamesBySiteUserId(@Param("siteUserId") long siteUserId);
}
