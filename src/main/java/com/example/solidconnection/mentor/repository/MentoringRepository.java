package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.mentor.domain.Mentoring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MentoringRepository extends JpaRepository<Mentoring, Long> {

    boolean existsByMentorIdAndMenteeId(long mentorId, long menteeId);

    @Query("""
            SELECT m FROM Mentoring m
            WHERE m.mentorId IN :mentorIds AND m.menteeId = :menteeId
            """)
    List<Mentoring> findAllByMentorIdInAndMenteeId(@Param("mentorIds") List<Long> mentorIds, @Param("menteeId") long menteeId);
}
