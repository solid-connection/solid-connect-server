package com.example.solidconnection.support.fixture;

import com.example.solidconnection.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepositoryForTest extends JpaRepository<Country, Long> {

    Optional<Country> findByCode(String code);
}
