package com.example.solidconnection.application.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record UnivApplyInfoResponse(

        @JsonProperty("firstChoiceUniversity")
        String firstChoiceUnivApplyInfo,

        @JsonProperty("secondChoiceUniversity")
        @JsonInclude(NON_NULL)
        String secondChoiceUnivApplyInfo,

        @JsonProperty("thirdChoiceUniversity")
        @JsonInclude(NON_NULL)
        String thirdChoiceUnivApplyInfo) {

    public static UnivApplyInfoResponse of(Application application, List<UnivApplyInfo> univApplyInfos) {
        Map<Long, String> univApplyInfoMap = univApplyInfos.stream()
                .collect(Collectors.toMap(
                        UnivApplyInfo::getId,
                        UnivApplyInfo::getKoreanName
                ));

        return new UnivApplyInfoResponse(
                univApplyInfoMap.get(application.getFirstChoiceUnivApplyInfoId()),
                application.getSecondChoiceUnivApplyInfoId() != null
                        ? univApplyInfoMap.get(application.getSecondChoiceUnivApplyInfoId()) : null,
                application.getThirdChoiceUnivApplyInfoId() != null
                        ? univApplyInfoMap.get(application.getThirdChoiceUnivApplyInfoId()) : null
        );
    }
}
