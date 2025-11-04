package com.example.solidconnection.term.repository;

import com.example.solidconnection.term.domain.Term;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepositoryForTest extends JpaRepository<Term, Long> {

    Optional<Term> findByName(String name);
}
