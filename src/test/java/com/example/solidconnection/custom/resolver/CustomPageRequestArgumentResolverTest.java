package com.example.solidconnection.custom.resolver;

import com.example.solidconnection.custom.request.CustomPageRequest;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@TestContainerSpringBootTest
@DisplayName("커스텀 페이지 요청 argument resolver 테스트")
class CustomPageRequestArgumentResolverTest {

    private static final String PAGE_PARAMETER = "page";
    private static final String SIZE_PARAMETER = "size";
    private static final String SORT_PARAMETER = "sort";
    private static final String NAME = "name";
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;

    @Autowired
    private CustomPageRequestArgumentResolver customPageRequestArgumentResolver;

    private MockHttpServletRequest request;
    private NativeWebRequest webRequest;
    private MethodParameter parameter;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        webRequest = new ServletWebRequest(request);
        parameter = mock(MethodParameter.class);
    }

    @Test
    void CustomPageRequest_타입의_파라미터를_지원한다() {
        // given
        given(parameter.getParameterType()).willReturn((Class) CustomPageRequest.class);

        // when & then
        assertThat(customPageRequestArgumentResolver.supportsParameter(parameter)).isTrue();
    }

    @Test
    void CustomPageRequest_타입이_아닌_파라미터는_지원하지_않는다() {
        // given
        given(parameter.getParameterType()).willReturn((Class) String.class);

        // when & then
        assertThat(customPageRequestArgumentResolver.supportsParameter(parameter)).isFalse();
    }

    @Nested
    class 페이지_파라미터_추출_테스트 {

        @Test
        void 페이지_파라미터가_없으면_기본값을_사용한다() {
            // when
            CustomPageRequest pageRequest = (CustomPageRequest) customPageRequestArgumentResolver
                    .resolveArgument(parameter, null, webRequest, null);

            // then
            assertThat(pageRequest.getPage()).isEqualTo(DEFAULT_PAGE);
            assertThat(pageRequest.getSize()).isEqualTo(DEFAULT_SIZE);
        }

        @Test
        void 페이지_파라미터가_있으면_해당_값을_사용한다() {
            // given
            int expectedPage = 2;
            int expectedSize = 20;
            request.setParameter(PAGE_PARAMETER, String.valueOf(expectedPage));
            request.setParameter(SIZE_PARAMETER, String.valueOf(expectedSize));

            // when
            CustomPageRequest pageRequest = (CustomPageRequest) customPageRequestArgumentResolver
                    .resolveArgument(parameter, null, webRequest, null);

            // then
            assertThat(pageRequest.getPage()).isEqualTo(expectedPage);
            assertThat(pageRequest.getSize()).isEqualTo(expectedSize);
        }

        @Test
        void 페이지_파라미터가_숫자가_아니면_기본값을_사용한다() {
            // given
            request.setParameter(PAGE_PARAMETER, "invalid");
            request.setParameter(SIZE_PARAMETER, "invalid");

            // when
            CustomPageRequest pageRequest = (CustomPageRequest) customPageRequestArgumentResolver
                    .resolveArgument(parameter, null, webRequest, null);

            // then
            assertThat(pageRequest.getPage()).isEqualTo(DEFAULT_PAGE);
            assertThat(pageRequest.getSize()).isEqualTo(DEFAULT_SIZE);
        }
    }

    @Nested
    class 정렬_파라미터_추출_테스트 {

        @Test
        void 정렬_파라미터가_없으면_정렬하지_않는다() {
            // when
            CustomPageRequest pageRequest = (CustomPageRequest) customPageRequestArgumentResolver
                    .resolveArgument(parameter, null, webRequest, null);

            // then
            assertThat(pageRequest.getSort().isSorted()).isFalse();
        }

        @Test
        void 정렬_파라미터가_있으면_해당_값으로_정렬한다() {
            // given
            request.addParameter(SORT_PARAMETER, NAME + ",asc");

            // when
            CustomPageRequest pageRequest = (CustomPageRequest) customPageRequestArgumentResolver
                    .resolveArgument(parameter, null, webRequest, null);

            // then
            assertThat(pageRequest.getSort().isSorted()).isTrue();
            assertThat(pageRequest.getSort().getOrderFor(NAME).getDirection()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        void 정렬_방향을_명시하지_않으면_기본값으로_오름차순을_사용한다() {
            // given
            request.addParameter(SORT_PARAMETER, NAME);

            // when
            CustomPageRequest pageRequest = (CustomPageRequest) customPageRequestArgumentResolver
                    .resolveArgument(parameter, null, webRequest, null);

            // then
            assertThat(pageRequest.getSort().isSorted()).isTrue();
            assertThat(pageRequest.getSort().getOrderFor(NAME).getDirection()).isEqualTo(Sort.Direction.ASC);
        }
    }
}
