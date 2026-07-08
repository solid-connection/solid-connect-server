package com.example.solidconnection.siteuser.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SchoolEmailRequest(
        @NotBlank(message = "학교 이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @Size(max = 100, message = "학교 이메일은 100자 이하여야 합니다")
        String schoolEmail
) {

}
