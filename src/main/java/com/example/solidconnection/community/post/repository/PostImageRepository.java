package com.example.solidconnection.community.post.repository;

import com.example.solidconnection.community.post.domain.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
}
