package com.example.solidconnection.unit.university.service;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.example.solidconnection.university.domain.LikedUniversity;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.dto.IsLikeResponse;
import com.example.solidconnection.university.dto.LikeResultResponse;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.service.UniversityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.example.solidconnection.university.service.UniversityService.LIKE_CANCELED_MESSAGE;
import static com.example.solidconnection.university.service.UniversityService.LIKE_SUCCESS_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("대학교 좋아요 서비스 테스트")
class UniversityLikeServiceTest {

    @InjectMocks
    private UniversityService universityService;

    @Mock
    private UniversityInfoForApplyRepository universityInfoForApplyRepository;

    @Mock
    private SiteUserRepository siteUserRepository;

    @Mock
    private LikedUniversityRepository likedUniversityRepository;

    private SiteUser testUser;
    private UniversityInfoForApply testUniversity;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testUniversity = createTestUniversity();
    }

    @Test
    @DisplayName("사용자가_특정_대학을_좋아요_상태인지_확인하면_True를_반환한다()")
    void shouldReturnTrueWhenUserLikedTheUniversity() {
        // given
        String email = testUser.getEmail();
        Long universityId = testUniversity.getId();

        when(siteUserRepository.getByEmail(email)).thenReturn(testUser);
        when(universityInfoForApplyRepository.getUniversityInfoForApplyById(universityId)).thenReturn(testUniversity);
        when(likedUniversityRepository.findBySiteUserAndUniversityInfoForApply(testUser, testUniversity))
                .thenReturn(Optional.of(new LikedUniversity()));

        // when
        IsLikeResponse response = universityService.getIsLiked(email, universityId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isLike()).isTrue();

        // verify
        verify(siteUserRepository).getByEmail(email);
        verify(universityInfoForApplyRepository).getUniversityInfoForApplyById(universityId);
        verify(likedUniversityRepository).findBySiteUserAndUniversityInfoForApply(testUser, testUniversity);
    }

    @Test
    @DisplayName("사용자가_특정_대학을_좋아요_상태인지_확인하면_False를_반환한다()")
    void shouldReturnFalseWhenUserNotLikedTheUniversity() {
        // given
        String email = testUser.getEmail();
        Long universityId = testUniversity.getId();

        when(siteUserRepository.getByEmail(email)).thenReturn(testUser);
        when(universityInfoForApplyRepository.getUniversityInfoForApplyById(universityId)).thenReturn(testUniversity);
        when(likedUniversityRepository.findBySiteUserAndUniversityInfoForApply(testUser, testUniversity))
                .thenReturn(Optional.empty());

        // when
        IsLikeResponse response = universityService.getIsLiked(email, universityId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isLike()).isFalse();

        // verify
        verify(siteUserRepository).getByEmail(email);
        verify(universityInfoForApplyRepository).getUniversityInfoForApplyById(universityId);
        verify(likedUniversityRepository).findBySiteUserAndUniversityInfoForApply(testUser, testUniversity);
    }

    @Test
    @DisplayName("사용자가_대학_좋아요를_추가하면_성공_메시지를_반환한다()")
    void shouldAddLikeWhenUserNotLikedTheUniversity() {
        // given
        String email = testUser.getEmail();
        Long universityId = testUniversity.getId();

        when(siteUserRepository.getByEmail(email)).thenReturn(testUser);
        when(universityInfoForApplyRepository.getUniversityInfoForApplyById(universityId)).thenReturn(testUniversity);
        when(likedUniversityRepository.findBySiteUserAndUniversityInfoForApply(testUser, testUniversity))
                .thenReturn(Optional.empty());

        // when
        LikeResultResponse response = universityService.likeUniversity(email, universityId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.result()).isEqualTo(LIKE_SUCCESS_MESSAGE);

        // verify
        verify(likedUniversityRepository).save(any(LikedUniversity.class));
    }

    @Test
    @DisplayName("사용자가_대학_좋아요를_취소하면_취소_메시지를_반환한다()")
    void shouldRemoveLikeWhenUserAlreadyLikedTheUniversity() {
        // given
        String email = testUser.getEmail();
        Long universityId = testUniversity.getId();

        when(siteUserRepository.getByEmail(email)).thenReturn(testUser);
        when(universityInfoForApplyRepository.getUniversityInfoForApplyById(universityId)).thenReturn(testUniversity);
        when(likedUniversityRepository.findBySiteUserAndUniversityInfoForApply(testUser, testUniversity))
                .thenReturn(Optional.of(new LikedUniversity(1L, testUniversity, testUser)));

        // when
        LikeResultResponse response = universityService.likeUniversity(email, universityId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.result()).isEqualTo(LIKE_CANCELED_MESSAGE);

        // verify
        verify(likedUniversityRepository).delete(any(LikedUniversity.class));
    }

    @Test
    @DisplayName("존재하지_않는_대학_ID로_좋아요_요청_시_예외를_반환한다()")
    void shouldThrowExceptionWhenUniversityNotFound() {
        // given
        String email = testUser.getEmail();
        Long invalidUniversityId = 999L;

        when(siteUserRepository.getByEmail(email)).thenReturn(testUser);
        when(universityInfoForApplyRepository.getUniversityInfoForApplyById(invalidUniversityId))
                .thenThrow(new CustomException(ErrorCode.UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> universityService.likeUniversity(email, invalidUniversityId));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND.getCode());

        // verify
        verify(siteUserRepository).getByEmail(email);
        verify(universityInfoForApplyRepository).getUniversityInfoForApplyById(invalidUniversityId);
    }

    private SiteUser createTestUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-10-21",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
    }

    private UniversityInfoForApply createTestUniversity() {
        return new UniversityInfoForApply(
                1L,
                "2025-1-a",
                "Test University",
                3,
                null,
                null,
                "4학기",
                "어학 요구사항",
                "3.0/4.5",
                "4.5",
                "지원 상세",
                "전공 상세",
                "기숙사 상세",
                "영어 강좌 상세",
                "기타 상세",
                null,
                null
        );
    }
}
