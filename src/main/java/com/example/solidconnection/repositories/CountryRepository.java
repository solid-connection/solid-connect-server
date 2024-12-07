package com.example.solidconnection.repositories;

import com.example.solidconnection.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByKoreanName(String koreanName);

    @Query("SELECT c FROM Country c WHERE c.koreanName IN :names")
    List<Country> findByKoreanNames(@Param(value = "names") List<String> names);

    @Query("SELECT c FROM Country c WHERE c.koreanName LIKE %:keyword%")
    List<Country> findByKoreanNameContaining(@Param("keyword") String keyword);
}
