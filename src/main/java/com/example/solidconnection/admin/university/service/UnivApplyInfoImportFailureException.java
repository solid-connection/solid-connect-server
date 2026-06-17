package com.example.solidconnection.admin.university.service;

import com.example.solidconnection.admin.university.dto.UnivApplyInfoImportResponse.CellError;
import java.util.List;

public class UnivApplyInfoImportFailureException extends RuntimeException {

    private final List<CellError> errors;

    public UnivApplyInfoImportFailureException(String message, CellError error) {
        this(message, List.of(error));
    }

    public UnivApplyInfoImportFailureException(String message, List<CellError> errors) {
        super(message);
        this.errors = errors;
    }

    public List<CellError> getErrors() {
        return errors;
    }
}
