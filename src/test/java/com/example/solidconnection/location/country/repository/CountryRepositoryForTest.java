package com.example.solidconnection.location.country.repository;

import com.example.solidconnection.location.country.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepositoryForTest extends JpaRepository<Country, Long> {

    Optional<Country> findByCode(String code);
}
