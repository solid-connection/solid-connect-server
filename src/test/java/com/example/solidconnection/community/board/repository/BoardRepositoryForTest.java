package com.example.solidconnection.community.board.repository;

import com.example.solidconnection.community.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardRepositoryForTest extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b LEFT JOIN FETCH b.postList WHERE b.code = :code")
    Optional<Board> findByCodeWithPosts(@Param("code") String code);
}
