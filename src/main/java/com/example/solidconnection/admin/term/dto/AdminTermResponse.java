package com.example.solidconnection.admin.term.dto;

import com.example.solidconnection.term.domain.Term;

public record AdminTermResponse(
        Long id,
        String label,
        boolean isCurrent
) {

    public static AdminTermResponse from(Term term) {
        return new AdminTermResponse(
                term.getId(),
                term.getName(),
                Boolean.TRUE.equals(term.getIsCurrent())
        );
    }
}
