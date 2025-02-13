package com.example.solidconnection.util;

import com.example.solidconnection.custom.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_PAGE;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_SIZE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("PagingUtils 테스트")
class PagingUtilsTest {

    @Test
    @DisplayName("유효한 페이지 번호와 크기가 주어지면 예외가 발생하지 않는다")
    void validateValidPageAndSize() {
        // given
        int validPage = 1;
        int validSize = 10;

        // when & then
        assertThatCode(() -> PagingUtils.validatePage(validPage, validSize))
                .doesNotThrowAnyException();
    }

    @Test
    void 페이지_번호가_1보다_작으면_예외_응답을_반환한다() {
        // given
        int invalidPage = 0;
        int validSize = 10;

        // when & then
        assertThatThrownBy(() -> PagingUtils.validatePage(invalidPage, validSize))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_PAGE.getMessage());
    }

    @Test
    void 페이지_크기가_1보다_작으면_예외_응답을_반환한다() {
        // given
        int validPage = 1;
        int invalidSize = 0;

        // when & then
        assertThatThrownBy(() -> PagingUtils.validatePage(validPage, invalidSize))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_SIZE.getMessage());
    }
}
