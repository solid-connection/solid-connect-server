package com.example.solidconnection.custom.resolver;

import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;

import static com.example.solidconnection.custom.resolver.CustomPageableHandlerMethodArgumentResolver.MAX_SIZE;
import static org.assertj.core.api.Assertions.assertThat;

@TestContainerSpringBootTest
@DisplayName("커스텀 페이지 요청 argument resolver 테스트")
class CustomPageableHandlerMethodArgumentResolverTest {

    private static final String PAGE_PARAMETER = "page";
    private static final String SIZE_PARAMETER = "size";
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;

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
    void 파라미터가_없으면_기본값을_사용한다() {
        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(DEFAULT_PAGE - 1);
        assertThat(pageable.getPageSize()).isEqualTo(DEFAULT_SIZE);
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

    @Test
    void 파라미터가_숫자가_아니면_기본값을_사용한다() {
        // given
        request.setParameter(PAGE_PARAMETER, "invalid");
        request.setParameter(SIZE_PARAMETER, "invalid");

        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(DEFAULT_PAGE - 1);
        assertThat(pageable.getPageSize()).isEqualTo(DEFAULT_SIZE);
    }

    @Test
    void 페이지_파라미터가_최소값보다_작으면_기본값을_사용한다() {
        // given
        request.setParameter(PAGE_PARAMETER, "0");

        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(DEFAULT_PAGE - 1);
        assertThat(pageable.getPageSize()).isEqualTo(DEFAULT_SIZE);
    }

    @Test
    void 페이지_파라미터가_음수이면_기본값을_사용한다() {
        // given
        request.setParameter(PAGE_PARAMETER, "-1");

        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(DEFAULT_PAGE - 1);
        assertThat(pageable.getPageSize()).isEqualTo(DEFAULT_SIZE);
    }

    @Test
    void 사이즈_파라미터가_최소값보다_작으면_기본값을_사용한다() {
        // given
        request.setParameter(SIZE_PARAMETER, "0");

        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(DEFAULT_PAGE - 1);
        assertThat(pageable.getPageSize()).isEqualTo(DEFAULT_SIZE);
    }

    @Test
    void 사이즈_파라미터가_최대값보다_크면_기본값을_사용한다() {
        // given
        request.setParameter(SIZE_PARAMETER, String.valueOf(MAX_SIZE + 1));

        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(DEFAULT_PAGE - 1);
        assertThat(pageable.getPageSize()).isEqualTo(DEFAULT_SIZE);
    }

    @Test
    void 사이즈_파라미터가_음수이면_기본값을_사용한다() {
        // given
        request.setParameter(SIZE_PARAMETER, "-1");

        // when
        Pageable pageable = customPageableHandlerMethodArgumentResolver
                .resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(DEFAULT_PAGE - 1);
        assertThat(pageable.getPageSize()).isEqualTo(DEFAULT_SIZE);
    }

    private static class TestController {

        public void pageableMethod(Pageable pageable) {
        }
    }
}
