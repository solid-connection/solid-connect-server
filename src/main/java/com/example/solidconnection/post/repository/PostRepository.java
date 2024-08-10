package com.example.solidconnection.post.repository;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.post.domain.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_ID;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"postImageList", "board", "siteUser"})
    Optional<Post> findPostById(Long id);

    default Post getByIdUsingEntityGraph(Long id) {
        return findPostById(id)
                .orElseThrow(() -> new CustomException(INVALID_POST_ID));
    }

    default Post getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(INVALID_POST_ID));
    }
}
