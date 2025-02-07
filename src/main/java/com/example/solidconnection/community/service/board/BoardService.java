package com.example.solidconnection.community.service.board;

import com.example.solidconnection.community.domain.board.Board;
import com.example.solidconnection.community.repository.board.BoardRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.community.domain.post.Post;
import com.example.solidconnection.community.dto.post.BoardFindPostResponse;
import com.example.solidconnection.type.BoardCode;
import com.example.solidconnection.type.PostCategory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_CATEGORY;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    @Transactional(readOnly = true)
    public List<BoardFindPostResponse> findPostsByCodeAndPostCategory(String code, String category) {

        String boardCode = validateCode(code);
        PostCategory postCategory = validatePostCategory(category);

        Board board = boardRepository.getByCodeUsingEntityGraph(boardCode);
        List<Post> postList = getPostListByPostCategory(board.getPostList(), postCategory);

        return BoardFindPostResponse.from(postList);
    }

    private String validateCode(String code) {
        try {
            return String.valueOf(BoardCode.valueOf(code));
        } catch (IllegalArgumentException ex) {
            throw new CustomException(ErrorCode.INVALID_BOARD_CODE);
        }
    }

    private PostCategory validatePostCategory(String category) {
        if (!EnumUtils.isValidEnum(PostCategory.class, category)) {
            throw new CustomException(INVALID_POST_CATEGORY);
        }
        return PostCategory.valueOf(category);
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
