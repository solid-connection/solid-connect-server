package com.example.solidconnection.community.board.repository;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.community.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_BOARD_CODE;

public interface BoardRepository extends JpaRepository<Board, String> {

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
