package com.example.solidconnection.country.repository;

import com.example.solidconnection.country.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    @Query("SELECT c FROM Country c WHERE c.koreanName IN :names")
    List<Country> findByKoreanNames(@Param(value = "names") List<String> names);
}
