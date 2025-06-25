package com.example.solidconnection.application.dto;

import com.example.solidconnection.university.dto.validation.ValidUnivApplyInfoChoice;

@ValidUnivApplyInfoChoice
public record UnivApplyInfoChoiceRequest(
        Long firstChoiceUniversityId,
        Long secondChoiceUniversityId,
        Long thirdChoiceUniversityId) {
    // todo: #345 프론트에 firstChoiceUnivApplyInfoId, secondChoiceUnivApplyInfoId, thirdChoiceUnivApplyInfoId로 요청 받는고 전달 후, 인자명 & bruno 변경 필요
}
