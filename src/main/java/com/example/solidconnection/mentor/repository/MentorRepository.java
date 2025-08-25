package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.location.region.domain.Region;
import com.example.solidconnection.mentor.domain.Mentor;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentorRepository extends JpaRepository<Mentor, Long> {

    boolean existsBySiteUserId(long siteUserId);

    Optional<Mentor> findBySiteUserId(long siteUserId);

    Slice<Mentor> findAllBy(Pageable pageable);

    @Query("""
           SELECT m FROM Mentor m
           JOIN University u ON m.universityId = u.id
           WHERE u.region = :region
           """)
    Slice<Mentor> findAllByRegion(@Param("region") Region region, Pageable pageable);

    @Query("""
           SELECT m FROM Mentor m
           WHERE m.id IN (
               SELECT mt.mentorId FROM Mentoring mt
               WHERE mt.menteeId = :menteeId
               AND mt.verifyStatus = :verifyStatus
           )
           """)
    Slice<Mentor> findApprovedMentorsByMenteeId(@Param("menteeId") long menteeId, @Param("verifyStatus") VerifyStatus verifyStatus, Pageable pageable);
}
