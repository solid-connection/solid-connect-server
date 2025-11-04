package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.location.region.domain.Region;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.MentorStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentorRepository extends JpaRepository<Mentor, Long> {

    boolean existsBySiteUserId(long siteUserId);

    Optional<Mentor> findBySiteUserId(long siteUserId);

    Slice<Mentor> findAllByMentorStatus(MentorStatus mentorStatus, Pageable pageable);

    @Query("""
           SELECT m FROM Mentor m
           JOIN University u ON m.universityId = u.id
           WHERE u.region = :region
             AND m.mentorStatus = :mentorStatus
           """)
    Slice<Mentor> findAllByRegionAndMentorStatus(@Param("region") Region region, @Param("mentorStatus") MentorStatus mentorStatus, Pageable pageable);
}
