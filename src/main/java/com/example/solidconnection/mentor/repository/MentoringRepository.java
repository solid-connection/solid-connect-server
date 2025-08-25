package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.mentor.domain.Mentoring;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentoringRepository extends JpaRepository<Mentoring, Long> {

    int countByMentorIdAndCheckedAtByMentorIsNull(long mentorId);

    boolean existsByMentorIdAndMenteeId(long mentorId, long menteeId);

    Slice<Mentoring> findAllByMentorId(long mentorId, Pageable pageable);

    @Query("""
           SELECT m FROM Mentoring m
           WHERE m.menteeId = :menteeId AND m.verifyStatus = :verifyStatus
           """)
    Slice<Mentoring> findAllByMenteeIdAndVerifyStatus(@Param("menteeId") long menteeId, @Param("verifyStatus") VerifyStatus verifyStatus, Pageable pageable);

    @Query("""
           SELECT m FROM Mentoring m
           WHERE m.mentorId IN :mentorIds AND m.menteeId = :menteeId
           """)
    List<Mentoring> findAllByMentorIdInAndMenteeId(@Param("mentorIds") List<Long> mentorIds, @Param("menteeId") long menteeId);

    @Query("""
           SELECT m FROM Mentoring m
           WHERE m.menteeId = :menteeId
           AND m.mentorId IN :mentorIds
           AND m.verifyStatus = :verifyStatus
           """)
    List<Mentoring> findApprovedMentoringsByMenteeIdAndMentorIds(@Param("menteeId") long menteeId, @Param("verifyStatus") VerifyStatus verifyStatus, @Param("mentorIds") List<Long> mentorIds);
}
