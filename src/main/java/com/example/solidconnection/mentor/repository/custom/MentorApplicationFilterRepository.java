package com.example.solidconnection.mentor.repository.custom;

import com.example.solidconnection.admin.dto.MentorApplicationSearchCondition;
import com.example.solidconnection.admin.dto.MentorApplicationSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MentorApplicationFilterRepository {

    Page<MentorApplicationSearchResponse> searchMentorApplications(MentorApplicationSearchCondition mentorApplicationSearchCondition, Pageable pageable);

}