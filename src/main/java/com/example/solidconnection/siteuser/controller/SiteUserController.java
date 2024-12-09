package com.example.solidconnection.siteuser.controller;

import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.siteuser.dto.MyPageUpdateResponse;
import com.example.solidconnection.siteuser.dto.NicknameUpdateRequest;
import com.example.solidconnection.siteuser.dto.NicknameUpdateResponse;
import com.example.solidconnection.siteuser.dto.ProfileImageUpdateResponse;
import com.example.solidconnection.siteuser.service.SiteUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RequiredArgsConstructor
@RequestMapping("/my-page")
@RestController
class SiteUserController {

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

    @PatchMapping("/update/profileImage")
    public ResponseEntity<ProfileImageUpdateResponse> updateProfileImage(
            Principal principal,
            @RequestParam(value = "file", required = false) MultipartFile imageFile) {
        ProfileImageUpdateResponse profileImageUpdateResponse = siteUserService.updateProfileImage(principal.getName(), imageFile);
        return ResponseEntity.ok().body(profileImageUpdateResponse);
    }

    @PatchMapping("/update/nickname")
    public ResponseEntity<NicknameUpdateResponse> updateNickname(
            Principal principal,
            @Valid @RequestBody NicknameUpdateRequest nicknameUpdateRequest) {
        NicknameUpdateResponse nicknameUpdateResponse = siteUserService.updateNickname(principal.getName(), nicknameUpdateRequest);
        return ResponseEntity.ok().body(nicknameUpdateResponse);
    }
}
