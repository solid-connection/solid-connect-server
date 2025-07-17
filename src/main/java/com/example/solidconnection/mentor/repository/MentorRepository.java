package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.mentor.domain.Mentor;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorRepository extends JpaRepository<Mentor, Long> {

    boolean existsBySiteUserId(long siteUserId);

    Optional<Mentor> findBySiteUserId(long siteUserId);

    Slice<Mentor> findAllBy(Pageable pageable);
}
