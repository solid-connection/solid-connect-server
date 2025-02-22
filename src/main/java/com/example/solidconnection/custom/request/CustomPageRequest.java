package com.example.solidconnection.custom.request;

import com.example.solidconnection.custom.exception.CustomException;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_PAGE;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_SIZE;

@Getter
public class CustomPageRequest {

    public static final int MIN_PAGE = 1;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 50;

    private final int page;
    private final int size;
    private Sort sort = Sort.unsorted();

    public CustomPageRequest(int page, int size) {
        validatePageParameters(page, size);
        this.page = page;
        this.size = size;
    }

    public CustomPageRequest(int page, int size, Sort sort) {
        this(page, size);
        this.sort = sort;
    }

    // 1-based -> 0-based 변환
    public Pageable toPageable() {
        return PageRequest.of(page - 1, size, sort);
    }

    private static void validatePageParameters(int page, int size) {
        if (page < MIN_PAGE) {
            throw new CustomException(INVALID_PAGE);
        }
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new CustomException(INVALID_SIZE);
        }
    }

    public static CustomPageRequest of(int page, int size, Sort sort) {
        return new CustomPageRequest(page, size, sort);
    }
}
