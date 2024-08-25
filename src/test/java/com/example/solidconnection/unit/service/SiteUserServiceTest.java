package com.example.solidconnection.unit.service;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.s3.S3Service;
import com.example.solidconnection.s3.UploadedFileUrlResponse;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.NicknameUpdateRequest;
import com.example.solidconnection.siteuser.dto.NicknameUpdateResponse;
import com.example.solidconnection.siteuser.dto.ProfileImageUpdateResponse;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.siteuser.service.SiteUserService;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.ImgType;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static com.example.solidconnection.custom.exception.ErrorCode.*;
import static com.example.solidconnection.siteuser.service.SiteUserService.NICKNAME_LAST_CHANGE_DATE_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("유저 서비스 테스트")
public class SiteUserServiceTest {
    @InjectMocks
    SiteUserService siteUserService;
    @Mock
    SiteUserRepository siteUserRepository;
    @Mock
    LikedUniversityRepository likedUniversityRepository;
    @Mock
    S3Service s3Service;

    private SiteUser siteUser;
    private MultipartFile imageFile;
    private UploadedFileUrlResponse uploadedFileUrlResponse;

    @BeforeEach
    void setUp() {
        siteUser = createSiteUser();
        imageFile = createMockImageFile();
        uploadedFileUrlResponse = createUploadedFileUrlResponse();

    }

    private SiteUser createSiteUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "http://example.com/profile.jpg",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
    }

    private MultipartFile createMockImageFile() {
        return new MockMultipartFile("file1", "test1.png",
                "image/png", "test image content 1".getBytes());

    }

    private UploadedFileUrlResponse createUploadedFileUrlResponse() {
        return new UploadedFileUrlResponse("https://s3.example.com/test1.png");
    }

    @Test
    void 프로필_이미지를_수정한다() {
        // Given
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(s3Service.uploadFile(imageFile, ImgType.PROFILE)).thenReturn(uploadedFileUrlResponse);

        // When
        ProfileImageUpdateResponse profileImageUpdateResponse =
                siteUserService.updateProfileImage(siteUser.getEmail(), imageFile);
        // Then
        assertEquals(profileImageUpdateResponse, ProfileImageUpdateResponse.from(siteUser));
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
        verify(s3Service, times(1)).deleteExProfile(siteUser.getEmail());
        verify(s3Service, times(1)).uploadFile(imageFile, ImgType.PROFILE);
        verify(siteUserRepository, times(1)).save(any(SiteUser.class));
    }

    @Test
    void 프로필_이미지를_수정할_때_이미지가_없다면_예외_응답을_반환한다() {
        // Given
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                siteUserService.updateProfileImage(siteUser.getEmail(), null));
        assertThat(exception.getMessage())
                .isEqualTo(PROFILE_IMAGE_NEEDED.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(PROFILE_IMAGE_NEEDED.getCode());
    }

    @Test
    void 닉네임을_수정한다() {
        // Given
        NicknameUpdateRequest nicknameUpdateRequest = new NicknameUpdateRequest("newNickname");
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);

        // When
        NicknameUpdateResponse nicknameUpdateResponse
                = siteUserService.updateNickname(siteUser.getEmail(), nicknameUpdateRequest);
        // Then
        assertEquals( nicknameUpdateResponse, NicknameUpdateResponse.from(siteUser));
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
        verify(siteUserRepository, times(1)).save(any(SiteUser.class));
    }

    @Test
    void 닉네임을_수정할_때_중복된_닉네임이라면_예외_응답을_반환한다() {
        // Given
        NicknameUpdateRequest nicknameUpdateRequest = new NicknameUpdateRequest("newNickname");
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(siteUserRepository.existsByNickname(nicknameUpdateRequest.nickname())).thenReturn(true);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                siteUserService.updateNickname(siteUser.getEmail(), nicknameUpdateRequest));
        assertThat(exception.getMessage())
                .isEqualTo(NICKNAME_ALREADY_EXISTED.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(NICKNAME_ALREADY_EXISTED.getCode());
    }

    @Test
    void 닉네임을_수정할_때_변경_가능_기한이_지나지_않았다면_예외_응답을_반환한다() {
        // Given
        NicknameUpdateRequest nicknameUpdateRequest = new NicknameUpdateRequest("newNickname");
        siteUser.setNicknameModifiedAt(LocalDateTime.now());
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                siteUserService.updateNickname(siteUser.getEmail(), nicknameUpdateRequest));

        String formatLastModifiedAt
                = String.format("(마지막 수정 시간 : %s)", NICKNAME_LAST_CHANGE_DATE_FORMAT.format(siteUser.getNicknameModifiedAt()));
        CustomException expectedException =  new CustomException(CAN_NOT_CHANGE_NICKNAME_YET, formatLastModifiedAt);
        assertThat(exception.getMessage()).isEqualTo(expectedException.getMessage());
        assertThat(exception.getCode()).isEqualTo(expectedException.getCode());
    }
}
