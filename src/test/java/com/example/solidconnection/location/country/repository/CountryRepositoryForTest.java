package com.example.solidconnection.location.country.repository;

import com.example.solidconnection.location.country.domain.Country;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepositoryForTest extends JpaRepository<Country, Long> {

    Optional<Country> findByCode(String code);
}
