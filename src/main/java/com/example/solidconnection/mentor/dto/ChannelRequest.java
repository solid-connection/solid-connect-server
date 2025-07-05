package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record ChannelRequest(
        @NotNull
        ChannelType type,

        @NotBlank
        @URL
        String url
) {
}
