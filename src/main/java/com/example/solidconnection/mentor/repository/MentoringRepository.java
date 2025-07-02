package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.mentor.domain.Mentoring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentoringRepository extends JpaRepository<Mentoring, Long> {

    List<Mentoring> findAllByMentorId(long mentorId);
    int countByMentorIdAndCheckedAtIsNull(long mentorId);
}
