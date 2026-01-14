package com.example.solidconnection.score.service;

import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.s3.domain.UploadType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.dto.GpaScoreRequest;
import com.example.solidconnection.score.dto.GpaScoreStatusResponse;
import com.example.solidconnection.score.dto.GpaScoreStatusesResponse;
import com.example.solidconnection.score.dto.LanguageTestScoreRequest;
import com.example.solidconnection.score.dto.LanguageTestScoreStatusResponse;
import com.example.solidconnection.score.dto.LanguageTestScoreStatusesResponse;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final GpaScoreRepository gpaScoreRepository;
    private final S3Service s3Service;
    private final LanguageTestScoreRepository languageTestScoreRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional
    public Long submitGpaScore(long siteUserId, GpaScoreRequest gpaScoreRequest, MultipartFile file) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(file, UploadType.GPA);
        Gpa gpa = new Gpa(gpaScoreRequest.gpa(), gpaScoreRequest.gpaCriteria(), uploadedFile.fileUrl());
        GpaScore newGpaScore = new GpaScore(gpa, siteUser);
        GpaScore savedNewGpaScore = gpaScoreRepository.save(newGpaScore);
        return savedNewGpaScore.getId();
    }

    @Transactional
    public Long submitLanguageTestScore(long siteUserId, LanguageTestScoreRequest languageTestScoreRequest, MultipartFile file) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(file, UploadType.LANGUAGE_TEST);
        LanguageTest languageTest = new LanguageTest(languageTestScoreRequest.languageTestType(),
                                                     languageTestScoreRequest.languageTestScore(), uploadedFile.fileUrl());
        LanguageTestScore newScore = new LanguageTestScore(languageTest, siteUser);
        LanguageTestScore savedNewScore = languageTestScoreRepository.save(newScore);
        return savedNewScore.getId();
    }

    @Transactional(readOnly = true)
    public GpaScoreStatusesResponse getGpaScoreStatus(long siteUserId) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        List<GpaScoreStatusResponse> gpaScoreStatusResponseList =
                gpaScoreRepository.findBySiteUserId(siteUser.getId())
                        .stream()
                        .map(GpaScoreStatusResponse::from)
                        .toList();

        return GpaScoreStatusesResponse.from(gpaScoreStatusResponseList);
    }

    @Transactional(readOnly = true)
    public LanguageTestScoreStatusesResponse getLanguageTestScoreStatus(long siteUserId) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        List<LanguageTestScore> languageTestScores = languageTestScoreRepository.findBySiteUserId(siteUser.getId());

        List<LanguageTestScoreStatusResponse> languageTestScoreStatusResponseList =
                languageTestScores.stream()
                        .map(LanguageTestScoreStatusResponse::from)
                        .collect(Collectors.toList());

        return LanguageTestScoreStatusesResponse.from(languageTestScoreStatusResponseList);
    }
}
