package com.example.solidconnection.util;

import com.example.solidconnection.custom.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_PAGE;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_SIZE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@DisplayName("PagingUtils 테스트")
class PagingUtilsTest {

    private static final int VALID_PAGE = 1;
    private static final int VALID_SIZE = 10;

    private static final int MIN_PAGE = 1;
    private static final int MAX_PAGE = 50;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;

    @Test
    @DisplayName("유효한 페이지 번호와 크기가 주어지면 예외가 발생하지 않는다")
    void validateValidPageAndSize() {
        // when & then
        assertThatCode(() -> PagingUtils.validatePage(VALID_PAGE, VALID_SIZE))
                .doesNotThrowAnyException();
    }

    @Test
    void 최소_페이지_번호보다_작으면_예외_응답을_반환한다() {
        // when & then
        assertThatCode(() -> PagingUtils.validatePage(MIN_PAGE - 1, VALID_SIZE))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_PAGE.getMessage());
    }

    @Test
    void 최대_페이지_번호보다_크면_예외_응답을_반환한다() {
        // when & then
        assertThatCode(() -> PagingUtils.validatePage(MAX_PAGE + 1, VALID_SIZE))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_PAGE.getMessage());
    }

    @Test
    void 최소_페이지_크기보다_작으면_예외_응답을_반환한다() {
        // when & then
        assertThatCode(() -> PagingUtils.validatePage(VALID_PAGE, MIN_SIZE - 1))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_SIZE.getMessage());
    }

    @Test
    void 최대_페이지_크기보다_크면_예외_응답을_반환한다() {
        // when & then
        assertThatCode(() -> PagingUtils.validatePage(VALID_PAGE, MAX_SIZE + 1))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_SIZE.getMessage());
    }
}
