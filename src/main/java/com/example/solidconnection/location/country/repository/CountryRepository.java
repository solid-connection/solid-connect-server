package com.example.solidconnection.location.country.repository;

import com.example.solidconnection.location.country.domain.Country;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CountryRepository extends JpaRepository<Country, Long> {

    @Query("SELECT c FROM Country c WHERE c.koreanName IN :names")
    List<Country> findByKoreanNames(@Param(value = "names") List<String> names);
}
