package com.example.solidconnection.util;

import com.example.solidconnection.custom.exception.CustomException;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_PAGE;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_SIZE;

public class PagingUtils {

    private static final int MIN_PAGE = 1;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;
    private static final int MAX_PAGE = 50;

    private PagingUtils() {
    }

    public static void validatePage(int page, int size) {
        if (page < MIN_PAGE || page > MAX_PAGE) {
            throw new CustomException(INVALID_PAGE);
        }
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new CustomException(INVALID_SIZE);
        }
    }
}
