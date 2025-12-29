package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.mentor.domain.MentorApplication;
import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorApplicationRepository extends JpaRepository<MentorApplication, Long> {

    boolean existsBySiteUserIdAndMentorApplicationStatusIn(long siteUserId, List<MentorApplicationStatus> mentorApplicationStatuses);

    void deleteAllBySiteUserId(long siteUserId);
}
