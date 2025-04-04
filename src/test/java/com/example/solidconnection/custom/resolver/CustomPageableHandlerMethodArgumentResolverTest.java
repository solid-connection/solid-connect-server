package com.example.solidconnection.custom.resolver;

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
    void 유효한_파라미터가_있으면_해당_값을_사용한다() {
        // given
        int expectedPage = 2;
        int expectedSize = 20;
        request.setParameter(PAGE_PARAMETER, String.valueOf(expectedPage));
        request.setParameter(SIZE_PARAMETER, String.valueOf(expectedSize));

        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(expectedPage - 1);
        assertThat(pageable.getPageSize()).isEqualTo(expectedSize);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidParameters")
    void 파라미터가_유효하지_않으면_기본_값을_사용한다(String testName, String pageParam, String sizeParam, int expectedPage, int expectedSize) {
        // given
        request.setParameter(PAGE_PARAMETER, pageParam);
        request.setParameter(SIZE_PARAMETER, sizeParam);

        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(expectedPage);
        assertThat(pageable.getPageSize()).isEqualTo(expectedSize);
    }

    static Stream<Arguments> provideInvalidParameters() {
        return Stream.of(
                Arguments.of("Page null", null, "20", DEFAULT_PAGE, 20),
                Arguments.of("Page 빈 문자열", "", "20", DEFAULT_PAGE, 20),
                Arguments.of("Page 0", "0", "20", DEFAULT_PAGE, 20),
                Arguments.of("Page 음수", "-1", "20", DEFAULT_PAGE, 20),
                Arguments.of("Page 문자열", "invalid", "20", DEFAULT_PAGE, 20),

                Arguments.of("Size null", "2", null, 1, DEFAULT_SIZE),
                Arguments.of("Size 빈 문자열", "2", "", 1, DEFAULT_SIZE),
                Arguments.of("Size 0", "2", "0", 1, DEFAULT_SIZE),
                Arguments.of("Size 음수", "2", "-1", 1, DEFAULT_SIZE),
                Arguments.of("Size 문자열", "2", "invalid", 1, DEFAULT_SIZE),
                Arguments.of("Size 최대값 초과", "2", String.valueOf(MAX_SIZE + 1), 1, MAX_SIZE),

                Arguments.of("모두 null", null, null, DEFAULT_PAGE, DEFAULT_SIZE),
                Arguments.of("모두 빈 문자열", "", "", DEFAULT_PAGE, DEFAULT_SIZE),
                Arguments.of("모두 0", "0", "0", DEFAULT_PAGE, DEFAULT_SIZE),
                Arguments.of("모두 음수", "-1", "-1", DEFAULT_PAGE, DEFAULT_SIZE),
                Arguments.of("모두 문자열", "invalid", "invalid", DEFAULT_PAGE, DEFAULT_SIZE)
        );
    }

    private static class TestController {

        public void pageableMethod(Pageable pageable) {
        }
    }
}
