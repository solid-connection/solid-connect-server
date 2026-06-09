package com.example.solidconnection.siteuser.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SchoolEmailRequest(
        @NotBlank(message = "학교 이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String schoolEmail
) {

}
