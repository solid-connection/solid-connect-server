package com.example.solidconnection.siteuser.controller;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.siteuser.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.example.solidconnection.common.exception.ErrorCode.DATA_INTEGRITY_VIOLATION;

@RequiredArgsConstructor
@RequestMapping("/my")
@RestController
class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPageInfo(
            @AuthorizedUser SiteUser siteUser
    ) {
        MyPageResponse myPageResponse = myPageService.getMyPageInfo(siteUser);
        return ResponseEntity.ok(myPageResponse);
    }

    @PatchMapping
    public ResponseEntity<Void> updateMyPageInfo(
            @AuthorizedUser SiteUser siteUser,
            @RequestParam(value = "file", required = false) MultipartFile imageFile,
            @RequestParam(value = "nickname", required = false) String nickname
    ) {
        try {
            myPageService.updateMyPageInfo(siteUser, imageFile, nickname);
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(determineErrorCode(e));
        }
    }

    private ErrorCode determineErrorCode(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage().toLowerCase();

        if (message.contains("unique") && message.contains("nickname")) {
            return ErrorCode.NICKNAME_ALREADY_EXISTED;
        }

        return ErrorCode.DATA_INTEGRITY_VIOLATION;
    }
}
