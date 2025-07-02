package com.example.solidconnection.score.service;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.s3.domain.ImgType;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final GpaScoreRepository gpaScoreRepository;
    private final S3Service s3Service;
    private final LanguageTestScoreRepository languageTestScoreRepository;

    @Transactional
    public Long submitGpaScore(SiteUser siteUser, GpaScoreRequest gpaScoreRequest, MultipartFile file) {
        UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(file, ImgType.GPA);
        Gpa gpa = new Gpa(gpaScoreRequest.gpa(), gpaScoreRequest.gpaCriteria(), uploadedFile.fileUrl());
        GpaScore newGpaScore = new GpaScore(gpa, siteUser);
        GpaScore savedNewGpaScore = gpaScoreRepository.save(newGpaScore);
        return savedNewGpaScore.getId();
    }

    @Transactional
    public Long submitLanguageTestScore(SiteUser siteUser, LanguageTestScoreRequest languageTestScoreRequest, MultipartFile file) {
        UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(file, ImgType.LANGUAGE_TEST);
        LanguageTest languageTest = new LanguageTest(languageTestScoreRequest.languageTestType(),
                languageTestScoreRequest.languageTestScore(), uploadedFile.fileUrl());
        LanguageTestScore newScore = new LanguageTestScore(languageTest, siteUser);
        LanguageTestScore savedNewScore = languageTestScoreRepository.save(newScore);
        return savedNewScore.getId();
    }

    @Transactional(readOnly = true)
    public GpaScoreStatusesResponse getGpaScoreStatus(SiteUser siteUser) {
        List<GpaScoreStatusResponse> gpaScoreStatusResponseList =
                gpaScoreRepository.findBySiteUserId(siteUser.getId())
                        .stream()
                        .map(GpaScoreStatusResponse::from)
                        .collect(Collectors.toList());

        return GpaScoreStatusesResponse.from(gpaScoreStatusResponseList);
    }

    @Transactional(readOnly = true)
    public LanguageTestScoreStatusesResponse getLanguageTestScoreStatus(SiteUser siteUser) {
        List<LanguageTestScore> languageTestScores = languageTestScoreRepository.findBySiteUserId(siteUser.getId());

        List<LanguageTestScoreStatusResponse> languageTestScoreStatusResponseList =
                languageTestScores.stream()
                        .map(LanguageTestScoreStatusResponse::from)
                        .collect(Collectors.toList());

        return LanguageTestScoreStatusesResponse.from(languageTestScoreStatusResponseList);
    }
}
