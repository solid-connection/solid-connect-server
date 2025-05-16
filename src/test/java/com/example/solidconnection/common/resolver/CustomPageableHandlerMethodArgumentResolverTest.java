package com.example.solidconnection.common.resolver;

import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestContainerSpringBootTest
@DisplayName("커스텀 페이지 요청 argument resolver 테스트")
class CustomPageableHandlerMethodArgumentResolverTest {

    private static final String PAGE_PARAMETER = "page";
    private static final String SIZE_PARAMETER = "size";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    @Autowired
    private CustomPageableHandlerMethodArgumentResolver customPageableHandlerMethodArgumentResolver;

    private MockHttpServletRequest request;
    private NativeWebRequest webRequest;
    private MethodParameter parameter;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        request = new MockHttpServletRequest();
        webRequest = new ServletWebRequest(request);
        Method method = TestController.class.getMethod("pageableMethod", Pageable.class);
        parameter = new MethodParameter(method, 0);
    }

    @Test
    void 유효한_페이지_파라미터가_있으면_해당_값을_사용한다() {
        // given
        int expectedPage = 2;
        request.setParameter(PAGE_PARAMETER, String.valueOf(expectedPage));

        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(expectedPage - 1);
        assertThat(pageable.getPageSize()).isEqualTo(DEFAULT_SIZE);
    }

    @Test
    void 유효한_사이즈_파라미터가_있으면_해당_값을_사용한다() {
        // given
        int expectedSize = 20;
        request.setParameter(SIZE_PARAMETER, String.valueOf(expectedSize));

        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(DEFAULT_PAGE);
        assertThat(pageable.getPageSize()).isEqualTo(expectedSize);
    }

    @Test
    void 사이즈_파라미터가_최대값을_초과하면_최대값을_사용한다() {
        // given
        request.setParameter(SIZE_PARAMETER, String.valueOf(MAX_SIZE + 1));

        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageSize()).isEqualTo(MAX_SIZE);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidParameters")
    void 페이지_파라미터가_유효하지_않으면_기본_값을_사용한다(String testName, String pageParam) {
        // given
        request.setParameter(PAGE_PARAMETER, pageParam);

        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(DEFAULT_PAGE);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidParameters")
    void 사이즈_파라미터가_유효하지_않으면_기본_값을_사용한다(String testName, String sizeParam) {
        // given
        request.setParameter(SIZE_PARAMETER, sizeParam);

        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageSize()).isEqualTo(DEFAULT_SIZE);
    }

    static Stream<Arguments> provideInvalidParameters() {
        return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("0", "0"),
                Arguments.of("음수", "-1"),
                Arguments.of("문자열", "invalid")
        );
    }

    private static class TestController {

        public void pageableMethod(Pageable pageable) {
        }
    }
}
