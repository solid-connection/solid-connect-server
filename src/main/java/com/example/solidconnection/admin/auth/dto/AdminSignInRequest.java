package com.example.solidconnection.admin.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminSignInRequest(
        @NotBlank String email,
        @NotBlank String password
) {

}
