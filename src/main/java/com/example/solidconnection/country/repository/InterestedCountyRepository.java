package com.example.solidconnection.country.repository;

import com.example.solidconnection.country.domain.InterestedCountry;
import com.example.solidconnection.siteuser.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestedCountyRepository extends JpaRepository<InterestedCountry, Long> {
    List<InterestedCountry> findAllBySiteUser(SiteUser siteUser);
}
