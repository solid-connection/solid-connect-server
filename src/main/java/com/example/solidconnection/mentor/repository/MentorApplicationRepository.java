package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.mentor.domain.MentorApplication;
import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import com.example.solidconnection.mentor.repository.custom.MentorApplicationFilterRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorApplicationRepository extends JpaRepository<MentorApplication, Long> , MentorApplicationFilterRepository {

    boolean existsBySiteUserIdAndMentorApplicationStatusIn(long siteUserId, List<MentorApplicationStatus> mentorApplicationStatuses);

    Optional<MentorApplication> findBySiteUserIdAndMentorApplicationStatus(long siteUserId, MentorApplicationStatus mentorApplicationStatus);
}
