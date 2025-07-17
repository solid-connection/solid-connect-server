package com.example.solidconnection.common.dto;

import java.util.List;
import org.springframework.data.domain.Slice;

public record SliceResponse<T>(
        List<T> content,
        int nextPageNumber
) {

    private static final int NO_NEXT_PAGE = -1;
    private static final int BASE_NUMBER = 1; // 1-based

    public static <T, R> SliceResponse<R> of(List<R> content, Slice<T> slice) {
        int nextPageNumber = slice.hasNext()
                ? slice.getNumber() + BASE_NUMBER + 1
                : NO_NEXT_PAGE;
        return new SliceResponse<>(content, nextPageNumber);
    }
}
