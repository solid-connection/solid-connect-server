package com.example.solidconnection.s3.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.solidconnection.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("업로드 디렉토리명 테스트")
class UploadDirectoryNameTest {

    @Nested
    class 대학_영문명_변환_테스트 {

        @Test
        void 대학_영문명의_공백을_언더스코어로_변환한다() {
            // given
            String englishName = "University of Tokyo";

            // when
            String directoryName = UploadDirectoryName.fromUniversityEnglishName(englishName);

            // then
            assertThat(directoryName)
                    .startsWith("university_of_tokyo_")
                    .matches("university_of_tokyo_[0-9a-f]{12}");
        }

        @Test
        void 특수문자를_제거하고_앰퍼샌드는_and로_변환한다() {
            // given
            String englishName = "Texas A&M University, Austin";

            // when
            String directoryName = UploadDirectoryName.fromUniversityEnglishName(englishName);

            // then
            assertThat(directoryName)
                    .startsWith("texas_a_and_m_university_austin_")
                    .matches("texas_a_and_m_university_austin_[0-9a-f]{12}");
        }

        @Test
        void 같은_slug로_변환되는_서로_다른_영문명은_다른_디렉토리명을_반환한다() {
            // given
            String englishName = "Texas A&M University";
            String normalizedCollisionName = "Texas A and M University";

            // when
            String directoryName = UploadDirectoryName.fromUniversityEnglishName(englishName);
            String collisionDirectoryName = UploadDirectoryName.fromUniversityEnglishName(normalizedCollisionName);

            // then
            assertThat(directoryName).isNotEqualTo(collisionDirectoryName);
        }

        @Test
        void 공백_문자열이면_예외가_발생한다() {
            // given
            String blankName = " ";

            // when & then
            assertThatThrownBy(() -> UploadDirectoryName.fromUniversityEnglishName(blankName))
                    .isInstanceOf(CustomException.class);
        }
    }
}
