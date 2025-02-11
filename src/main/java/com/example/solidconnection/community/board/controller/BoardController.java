package com.example.solidconnection.community.board.controller;

import com.example.solidconnection.community.post.dto.PostListResponse;
import com.example.solidconnection.community.post.service.PostQueryService;
import com.example.solidconnection.type.BoardCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {

    private final PostQueryService postQueryService;

    // todo: 회원별로 접근 가능한 게시판 목록 조회 기능 개발
    @GetMapping
    public ResponseEntity<?> findAccessibleCodes() {
        List<String> accessibleCodeList = new ArrayList<>();
        for (BoardCode boardCode : BoardCode.values()) {
            accessibleCodeList.add(String.valueOf(boardCode));
        }
        return ResponseEntity.ok().body(accessibleCodeList);
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> findPostsByCodeAndCategory(
            @PathVariable(value = "code") String code,
            @RequestParam(value = "category", defaultValue = "전체") String category) {
        List<PostListResponse> postsByCodeAndPostCategory = postQueryService
                .findPostsByCodeAndPostCategory(code, category);
        return ResponseEntity.ok().body(postsByCodeAndPostCategory);
    }
}
