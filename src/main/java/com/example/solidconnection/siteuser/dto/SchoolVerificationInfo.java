package com.example.solidconnection.siteuser.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SchoolVerificationInfo {

    private String schoolEmail;
    private Long homeUniversityId;
    private String code;
}
