package com.example.solidconnection.admin.university.dto;

import java.util.List;

public record UnivApplyInfoImportResponse(
        int successCount,
        List<FailedRow> failedRows
) {

    public record FailedRow(
            int rowNumber,
            String reason
    ) {
    }
}
