package com.example.solidconnection.admin.university.dto;

import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.UnivApplyInfoColumn;
import java.util.Arrays;
import java.util.List;

public record UnivApplyInfoFieldResponse(
        List<FieldInfo> structuredFields,
        List<String> languageTestTypes
) {

    public record FieldInfo(
            String field,
            List<String> aliases
    ) {
    }

    public static UnivApplyInfoFieldResponse of() {
        List<FieldInfo> fields = Arrays.stream(UnivApplyInfoColumn.values())
                .map(col -> new FieldInfo(col.getFieldName(), col.getAliases()))
                .toList();
        List<String> testTypes = Arrays.stream(LanguageTestType.values())
                .map(Enum::name)
                .toList();
        return new UnivApplyInfoFieldResponse(fields, testTypes);
    }
}
