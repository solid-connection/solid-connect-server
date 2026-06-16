package com.example.solidconnection.admin.university.dto;

import java.util.List;

public record UnivApplyInfoImportResponse(
        int successCount,
        List<FailedRow> failedRows,
        List<String> createdUniversities
) {

    public record FailedRow(
            int rowNumber,
            String reason
    ) {
    }
}
