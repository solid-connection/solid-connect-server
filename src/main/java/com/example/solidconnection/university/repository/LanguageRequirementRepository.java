package com.example.solidconnection.university.repository;

import com.example.solidconnection.university.domain.LanguageRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguageRequirementRepository extends JpaRepository<LanguageRequirement, Long> {
}
