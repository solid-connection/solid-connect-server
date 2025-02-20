package com.example.solidconnection.siteuser.controller;

import com.example.solidconnection.siteuser.dto.NicknameExistsResponse;
import com.example.solidconnection.siteuser.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/users")
@RestController
public class SiteUserController {

    private final SiteUserService siteUserService;

    @GetMapping("/exists")
    public ResponseEntity<NicknameExistsResponse> existsByNickname(@RequestParam("nickname") String nickname) {
        NicknameExistsResponse nicknameExistsResponse = siteUserService.existsByNickname(nickname);
        return ResponseEntity.ok(nicknameExistsResponse);
    }
}
