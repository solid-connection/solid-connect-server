package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.mentor.domain.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelRepositoryForTest extends JpaRepository<Channel, Long> {

    List<Channel> findAllByMentorId(long mentorId);
}
