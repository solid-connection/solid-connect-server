package com.example.solidconnection.board.service;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.dto.BoardFindPostResponse;
import com.example.solidconnection.type.BoardCode;
import com.example.solidconnection.type.PostCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    private String validateCode(String code) {
        try {
            return String.valueOf(BoardCode.valueOf(code));
        } catch (IllegalArgumentException ex) {
            throw new CustomException(ErrorCode.INVALID_BOARD_CODE);
        }
    }

    private PostCategory validatePostCategory(String postCategory) {
        try {
            return PostCategory.valueOf(postCategory);
        } catch (IllegalArgumentException ex) {
            throw new CustomException(ErrorCode.INVALID_POST_CATEGORY);
        }
    }

    @Transactional(readOnly = true)
    public List<BoardFindPostResponse> findPostsByCodeAndPostCategory(String code, String category) {

        String boardCode = validateCode(code);
        PostCategory postCategory = validatePostCategory(category);

        Board board = boardRepository.getByCodeUsingEntityGraph(boardCode);
        List<Post> postList = getPostListByPostCategory(board.getPostList(), postCategory);

        return BoardFindPostResponse.from(postList);
    }

    private List<Post> getPostListByPostCategory(List<Post> postList, PostCategory postCategory) {
        if (postCategory.equals(PostCategory.전체)) {
            return postList;
        }
        return postList.stream()
                .filter(post -> post.getCategory().equals(postCategory))
                .collect(Collectors.toList());
    }
}
