package com.example.solidconnection.admin.university.dto;

import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.UnivApplyInfoColumn;
import java.util.Arrays;
import java.util.List;

public record UnivApplyInfoFieldResponse(
        List<String> structuredFields,
        List<String> languageTestTypes
) {

    public static UnivApplyInfoFieldResponse of() {
        List<String> fields = Arrays.stream(UnivApplyInfoColumn.values())
                .map(UnivApplyInfoColumn::getFieldName)
                .toList();
        List<String> testTypes = Arrays.stream(LanguageTestType.values())
                .map(Enum::name)
                .toList();
        return new UnivApplyInfoFieldResponse(fields, testTypes);
    }
}
