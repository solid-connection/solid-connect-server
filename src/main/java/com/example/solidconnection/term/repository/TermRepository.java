package com.example.solidconnection.term.repository;

import com.example.solidconnection.term.domain.Term;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {

    Optional<Term> findByIsCurrentTrue();

    Optional<Term> findByName(String name);
}
