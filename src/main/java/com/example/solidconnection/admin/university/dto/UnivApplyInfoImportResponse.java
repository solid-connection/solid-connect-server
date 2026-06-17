package com.example.solidconnection.admin.university.dto;

import java.util.List;

public record UnivApplyInfoImportResponse(
        int successCount,
        List<String> createdUniversities
) {
}
