package com.example.solidconnection.university.dto;

import java.util.List;

public record UnivApplyInfoRecommendsResponse(
        List<UnivApplyInfoPreviewResponse> recommendedUniversities) {
    // todo: #345 프론트에 recommendedUnivApplyInfos 로 응답한다고 전달 후, 인자명 변경 필요
}
