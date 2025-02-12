package com.example.solidconnection.siteuser.controller;

import com.example.solidconnection.custom.resolver.AuthorizedUser;
import com.example.solidconnection.siteuser.domain.SiteUser;
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

@RequiredArgsConstructor
@RequestMapping("/my-page")
@RestController
class SiteUserController {

    private final SiteUserService siteUserService;

    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPageInfo(
            @AuthorizedUser SiteUser siteUser
    ) {
        MyPageResponse myPageResponse = siteUserService.getMyPageInfo(siteUser);
        return ResponseEntity.ok(myPageResponse);
    }

    @GetMapping("/update")
    public ResponseEntity<MyPageUpdateResponse> getMyPageInfoToUpdate(
            @AuthorizedUser SiteUser siteUser
    ) {
        MyPageUpdateResponse myPageUpdateDto = siteUserService.getMyPageInfoToUpdate(siteUser);
        return ResponseEntity.ok(myPageUpdateDto);
    }

    @PatchMapping("/update/profileImage")
    public ResponseEntity<ProfileImageUpdateResponse> updateProfileImage(
            @AuthorizedUser SiteUser siteUser,
            @RequestParam(value = "file", required = false) MultipartFile imageFile
    ) {
        ProfileImageUpdateResponse profileImageUpdateResponse = siteUserService.updateProfileImage(siteUser, imageFile);
        return ResponseEntity.ok().body(profileImageUpdateResponse);
    }

    @PatchMapping("/update/nickname")
    public ResponseEntity<NicknameUpdateResponse> updateNickname(
            @AuthorizedUser SiteUser siteUser,
            @Valid @RequestBody NicknameUpdateRequest nicknameUpdateRequest
    ) {
        NicknameUpdateResponse nicknameUpdateResponse = siteUserService.updateNickname(siteUser, nicknameUpdateRequest);
        return ResponseEntity.ok().body(nicknameUpdateResponse);
    }
}
