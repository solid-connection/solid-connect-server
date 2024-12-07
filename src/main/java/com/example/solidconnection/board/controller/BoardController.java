package com.example.solidconnection.board.controller;

import com.example.solidconnection.board.service.BoardService;
import com.example.solidconnection.post.dto.BoardFindPostResponse;
import com.example.solidconnection.type.BoardCode;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.example.solidconnection.config.swagger.SwaggerConfig.ACCESS_TOKEN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/communities")
@SecurityRequirements
@SecurityRequirement(name = ACCESS_TOKEN)
public class BoardController {

    private final BoardService boardService;

    // todo: 회원별로 접근 가능한 게시판 목록 조회 기능 개발
    @GetMapping()
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

        List<BoardFindPostResponse> postsByCodeAndPostCategory = boardService
                .findPostsByCodeAndPostCategory(code, category);
        return ResponseEntity.ok().body(postsByCodeAndPostCategory);
    }
}
