package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.mentor.domain.Mentoring;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentoringRepository extends JpaRepository<Mentoring, Long> {

    boolean existsByMentorIdAndMenteeId(long mentorId, long menteeId);
}
