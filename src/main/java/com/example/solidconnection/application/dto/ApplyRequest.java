package com.example.solidconnection.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ApplyRequest(

        @NotNull(message = "gpa score id를 입력해주세요.")
        Long gpaScoreId,

        @NotNull(message = "language test score id를 입력해주세요.")
        Long languageTestScoreId,

        @Valid
        UnivApplyInfoChoiceRequest universityChoiceRequest // todo:  #345 프론트에 univApplyInfoChoiceRequest 로 요청 받는다고 전달 후, 인자명 & bruno 변경 필요
) {
}
