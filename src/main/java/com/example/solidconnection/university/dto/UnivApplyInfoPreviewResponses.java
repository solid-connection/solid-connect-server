package com.example.solidconnection.university.dto;

import java.util.List;

public record UnivApplyInfoPreviewResponses(
        List<UnivApplyInfoPreviewResponse> univApplyInfoPreviews
        // todo: #345 응답 형식으로 바로 배열이 아니라, univApplyInfoPreviews로 감싸 응답한다고 전달 후, 코드 변경 필요
) {

}
