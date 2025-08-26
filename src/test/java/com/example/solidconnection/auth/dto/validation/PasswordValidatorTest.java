package com.example.solidconnection.auth.dto.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("비밀번호 유효성 검사 테스트")
class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();

    @Test
    void 정상_패턴이면_true를_반환한다() {
        assertThat(validator.isValid("abcd123!", null)).isTrue();
    }

    @Test
    void 숫자가_없으면_false를_반환한다() {
        assertThat(validator.isValid("abcdefg!", null)).isFalse();
    }

    @Test
    void 영문자가_없으면_false를_반환한다() {
        assertThat(validator.isValid("1234567!", null)).isFalse();
    }

    @Test
    void 특수문자가_없으면_false를_반환한다() {
        assertThat(validator.isValid("abcd1234", null)).isFalse();
    }

    @Test
    void 공백을_포함하면_false를_반환한다() {
        assertThat(validator.isValid("abcd123! ", null)).isFalse();
    }

    @Test
    void 길이가_8자_미만이면_false를_반환한다() {
        assertThat(validator.isValid("ab1!ab", null)).isFalse();
    }
}
