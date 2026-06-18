package com.example.solidconnection.term.repository;

import com.example.solidconnection.term.domain.Term;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TermRepository extends JpaRepository<Term, Long> {

    Optional<Term> findByIsCurrentTrue();

    Optional<Term> findByName(String name);

    @Modifying
    @Query("UPDATE Term t SET t.isCurrent = null WHERE t.isCurrent = true")
    void deactivateCurrentTerm();
}
