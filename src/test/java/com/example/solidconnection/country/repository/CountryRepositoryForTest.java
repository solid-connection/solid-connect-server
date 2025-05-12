package com.example.solidconnection.country.repository;

import com.example.solidconnection.country.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepositoryForTest extends JpaRepository<Country, Long> {

    Optional<Country> findByCode(String code);
}
