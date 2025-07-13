package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.mentor.domain.Channel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepositoryForTest extends JpaRepository<Channel, Long> {

    List<Channel> findAllByMentorId(long mentorId);
}
