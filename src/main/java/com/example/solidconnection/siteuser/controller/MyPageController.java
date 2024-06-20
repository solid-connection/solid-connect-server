package com.example.solidconnection.siteuser.controller;

import com.example.solidconnection.custom.response.CustomResponse;
import com.example.solidconnection.custom.response.DataResponse;
import com.example.solidconnection.custom.response.StatusResponse;
import com.example.solidconnection.siteuser.dto.MyPageDto;
import com.example.solidconnection.siteuser.dto.MyPageUpdateDto;
import com.example.solidconnection.siteuser.service.MyPageService;
import com.example.solidconnection.university.dto.UniversityPreviewDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/my-page")
@RestController
class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public CustomResponse getMyPageInfo(Principal principal) {
        MyPageDto myPageDto = myPageService.getMyPageInfo(principal.getName());
        return new DataResponse<>(myPageDto);
    }

    @GetMapping("/update")
    public CustomResponse getMyPageInfoToUpdate(Principal principal) {
        MyPageUpdateDto myPageUpdateDto = myPageService.getMyPageInfoToUpdate(principal.getName());
        return new DataResponse<>(myPageUpdateDto);
    }

    @PatchMapping("/update")
    public CustomResponse updateMyPageInfo(
            Principal principal,
            @Valid @RequestBody MyPageUpdateDto myPageUpdateDto) {
        myPageService.update(principal.getName(), myPageUpdateDto);
        return new StatusResponse(true);
    }

    @GetMapping("/wish-university")
    public CustomResponse getMyWishUniversity(Principal principal) {
        List<UniversityPreviewDto> wishUniversities = myPageService.getWishUniversity(principal.getName());
        return new DataResponse<>(wishUniversities);
    }
}
