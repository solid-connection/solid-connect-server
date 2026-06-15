package com.example.solidconnection.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.solidconnection.common.exception.CustomException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("마크다운 표 파서 테스트")
class MarkdownTableParserTest {

    private final MarkdownTableParser parser = new MarkdownTableParser();

    @Nested
    class 정상_파싱 {

        @Test
        void 헤더와_데이터_행을_올바르게_파싱한다() {
            String markdown = """
                    | 대학명 | 인원 | TOEIC |
                    |--------|------|-------|
                    | MIT | 2 | 800 |
                    | 하버드 | 3 |  |
                    """;

            List<Map<String, String>> rows = parser.parse(markdown);

            assertThat(rows).hasSize(2);
            assertThat(rows.get(0))
                    .containsEntry("대학명", "MIT")
                    .containsEntry("인원", "2")
                    .containsEntry("TOEIC", "800");
            assertThat(rows.get(1))
                    .containsEntry("대학명", "하버드")
                    .containsEntry("인원", "3")
                    .doesNotContainKey("TOEIC");
        }

        @Test
        void 중간_빈_셀이_있어도_이후_컬럼이_올바르게_매핑된다() {
            String markdown = """
                    | 대학명 | 인원 | TOEIC |
                    |--------|------|-------|
                    | MIT |  | 800 |
                    """;

            List<Map<String, String>> rows = parser.parse(markdown);

            assertThat(rows.get(0))
                    .containsEntry("대학명", "MIT")
                    .doesNotContainKey("인원")
                    .containsEntry("TOEIC", "800");
        }

        @Test
        void 빈_셀은_결과_맵에_포함되지_않는다() {
            String markdown = """
                    | 대학명 | 인원 |
                    |--------|------|
                    | MIT |  |
                    """;

            List<Map<String, String>> rows = parser.parse(markdown);

            assertThat(rows.get(0))
                    .containsKey("대학명")
                    .doesNotContainKey("인원");
        }
    }

    @Nested
    class 구조_검증 {

        @Test
        void 구분자_행이_없으면_예외를_던진다() {
            String markdown = """
                    | 대학명 | 인원 |
                    | MIT | 2 |
                    """;

            assertThatThrownBy(() -> parser.parse(markdown))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 데이터_행이_없으면_예외를_던진다() {
            String markdown = """
                    | 대학명 | 인원 |
                    |--------|------|
                    """;

            assertThatThrownBy(() -> parser.parse(markdown))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 헤더와_구분자만_있으면_예외를_던진다() {
            String markdown = "| 대학명 |";

            assertThatThrownBy(() -> parser.parse(markdown))
                    .isInstanceOf(CustomException.class);
        }
    }
}
