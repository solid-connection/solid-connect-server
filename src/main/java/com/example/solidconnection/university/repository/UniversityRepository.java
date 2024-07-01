package com.example.solidconnection.university.repository;

import com.example.solidconnection.entity.University;
import com.example.solidconnection.university.repository.custom.UniversityRepositoryForFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long>, UniversityRepositoryForFilter {

    @Query("SELECT u FROM University u WHERE u.country.code IN :countryCodes OR u.region.code IN :regionCodes")
    List<University> findByCountryCodeInOrRegionCodeIn(@Param("countryCodes") List<String> countryCodes, @Param("regionCodes") List<String> regionCodes);
}
