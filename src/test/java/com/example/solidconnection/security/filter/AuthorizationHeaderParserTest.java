package com.example.solidconnection.security.filter;

import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestContainerSpringBootTest
class AuthorizationHeaderParserTest {

    @Autowired
    private AuthorizationHeaderParser authorizationHeaderParser;

    @Nested
    class 요청으로부터_토큰을_추출한다 {

        @Test
        void 지정한_형식의_토큰이_있으면_토큰을_반환한다() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            String token = "token";
            request.addHeader("Authorization", "Bearer " + token);

            // when
            Optional<String> extractedToken = authorizationHeaderParser.parseToken(request);

            // then
            assertThat(extractedToken).get().isEqualTo(token);
        }

        @Test
        void 형식에_맞는_토큰이_없으면_빈_값을_반환한다() {
            // given
            MockHttpServletRequest noHeader = new MockHttpServletRequest();
            MockHttpServletRequest wrongPrefix = new MockHttpServletRequest();
            wrongPrefix.addHeader("Authorization", "Wrong token");
            MockHttpServletRequest emptyToken = new MockHttpServletRequest();
            emptyToken.addHeader("Authorization", "Bearer ");

            // when & then
            assertAll(
                    () -> assertThat(authorizationHeaderParser.parseToken(noHeader)).isEmpty(),
                    () -> assertThat(authorizationHeaderParser.parseToken(wrongPrefix)).isEmpty(),
                    () -> assertThat(authorizationHeaderParser.parseToken(emptyToken)).isEmpty()
            );
        }
    }
}
