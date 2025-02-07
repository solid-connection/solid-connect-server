package com.example.solidconnection.community.board.repository;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_BOARD_CODE;

@Repository
public interface BoardRepository extends JpaRepository<Board, String> {

    @EntityGraph(attributePaths = {"postList"})
    Optional<Board> findBoardByCode(@Param("code") String code);

    default Board getByCodeUsingEntityGraph(String code) {
        return findBoardByCode(code)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_BOARD_CODE));
    }

    default Board getByCode(String code) {
        return findById(code)
                .orElseThrow(() -> new CustomException(INVALID_BOARD_CODE));
    }
}
