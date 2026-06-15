package com.example.solidconnection.application.dto;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.ApplicationChoice;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record UnivApplyInfoResponse(List<String> choices) {

    public static UnivApplyInfoResponse of(Application application, List<UnivApplyInfo> univApplyInfos) {
        Map<Long, String> nameById = univApplyInfos.stream()
                .collect(Collectors.toMap(UnivApplyInfo::getId, UnivApplyInfo::getKoreanName));

        List<String> choiceNames = application.getChoices().stream()
                .sorted(Comparator.comparingInt(ApplicationChoice::getChoiceOrder))
                .map(choice -> nameById.get(choice.getUnivApplyInfoId()))
                .toList();

        return new UnivApplyInfoResponse(choiceNames);
    }
}
