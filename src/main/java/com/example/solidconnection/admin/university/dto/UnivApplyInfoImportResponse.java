package com.example.solidconnection.admin.university.dto;

import java.util.List;

public record UnivApplyInfoImportResponse(
        int successCount,
        List<FailedRow> failedRows,
        List<String> createdUniversities
) {

    public record FailedRow(
            int rowNumber,
            String reason,
            List<CellError> errors
    ) {

        public FailedRow(int rowNumber, String reason) {
            this(rowNumber, reason, List.of());
        }
    }

    public record CellError(
            String header,
            String field,
            String value,
            String code,
            String message
    ) {
    }
}
