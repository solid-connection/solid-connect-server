package com.example.solidconnection.siteuser.controller;


import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.siteuser.dto.LocationUpdateRequest;
import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.siteuser.dto.PasswordUpdateRequest;
import com.example.solidconnection.siteuser.service.MyPageService;
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
@RequestMapping("/my")
@RestController
class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPageInfo(
            @AuthorizedUser long siteUserId
    ) {
        MyPageResponse myPageResponse = myPageService.getMyPageInfo(siteUserId);
        return ResponseEntity.ok(myPageResponse);
    }

    @PatchMapping
    public ResponseEntity<Void> updateMyPageInfo(
            @AuthorizedUser long siteUserId,
            @RequestParam(value = "file", required = false) MultipartFile imageFile,
            @RequestParam(value = "nickname", required = false) String nickname
    ) {
        myPageService.updateMyPageInfo(siteUserId, imageFile, nickname);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @AuthorizedUser long siteUserId,
            @RequestBody @Valid PasswordUpdateRequest request
    ) {
        myPageService.updatePassword(siteUserId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/interested-location")
    public ResponseEntity<Void> updateLocation(
            @AuthorizedUser long siteUserId,
            @RequestBody @Valid LocationUpdateRequest request
    ) {
        myPageService.updateLocation(siteUserId, request);
        return ResponseEntity.ok().build();
    }
}
