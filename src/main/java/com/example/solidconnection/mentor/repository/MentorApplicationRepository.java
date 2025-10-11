package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.mentor.domain.MentorApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorApplicationRepository extends JpaRepository<MentorApplication, Long> {

    boolean existsBySiteUserId(long siteUserId);
}
