package com.example.solidconnection.university.repository;

import com.example.solidconnection.university.domain.HomeUniversity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeUniversityRepository extends JpaRepository<HomeUniversity, Long> {

    List<HomeUniversity> findAllByIdIn(List<Long> ids);

    Optional<HomeUniversity> findByName(String name);
}
