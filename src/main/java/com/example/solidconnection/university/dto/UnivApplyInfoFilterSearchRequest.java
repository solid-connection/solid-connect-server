package com.example.solidconnection.university.dto;

import com.example.solidconnection.university.domain.LanguageTestType;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UnivApplyInfoFilterSearchRequest(

        @NotNull(message = "어학 시험 종류를 선택해주세요.")
        LanguageTestType languageTestType,
        String testScore,
        List<String> countryCode
) {

}
