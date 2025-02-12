package com.example.solidconnection.community.board.controller;

import com.example.solidconnection.type.BoardCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/communities")
public class BoardController {

    // todo: 회원별로 접근 가능한 게시판 목록 조회 기능 개발
    @GetMapping()
    public ResponseEntity<?> findAccessibleCodes() {
        List<String> accessibleCodeList = new ArrayList<>();
        for (BoardCode boardCode : BoardCode.values()) {
            accessibleCodeList.add(String.valueOf(boardCode));
        }
        return ResponseEntity.ok().body(accessibleCodeList);
    }
}
