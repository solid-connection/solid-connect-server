package com.example.solidconnection.siteuser.controller;

import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.siteuser.dto.MyPageUpdateRequest;
import com.example.solidconnection.siteuser.dto.MyPageUpdateResponse;
import com.example.solidconnection.siteuser.service.SiteUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequiredArgsConstructor
@RequestMapping("/my-page")
@RestController
class SiteUserController implements SiteUserControllerSwagger {

    private final SiteUserService siteUserService;

    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPageInfo(Principal principal) {
        MyPageResponse myPageResponse = siteUserService.getMyPageInfo(principal.getName());
        return ResponseEntity
                .ok(myPageResponse);
    }

    @GetMapping("/update")
    public ResponseEntity<MyPageUpdateResponse> getMyPageInfoToUpdate(Principal principal) {
        MyPageUpdateResponse myPageUpdateDto = siteUserService.getMyPageInfoToUpdate(principal.getName());
        return ResponseEntity
                .ok(myPageUpdateDto);
    }

    @PatchMapping("/update")
    public ResponseEntity<MyPageUpdateResponse> updateMyPageInfo(
            Principal principal,
            @Valid @RequestBody MyPageUpdateRequest myPageUpdateDto) {
        MyPageUpdateResponse myPageUpdateResponse = siteUserService.update(principal.getName(), myPageUpdateDto);
        return ResponseEntity
                .ok(myPageUpdateResponse);
    }
}
