package com.example.solidconnection.location.country.repository;

import com.example.solidconnection.location.country.domain.Country;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {

    List<Country> findAllByKoreanNameIn(List<String> koreanNames);
}
