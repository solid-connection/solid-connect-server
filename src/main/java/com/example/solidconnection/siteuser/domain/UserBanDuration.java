package com.example.solidconnection.siteuser.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UserBanDuration {
    ONE_DAY(1),
    THREE_DAYS(3),
    SEVEN_DAYS(7);

    private final int days;
}
