package com.example.solidconnection.score.service;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.s3.S3Service;
import com.example.solidconnection.s3.UploadedFileUrlResponse;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.dto.GpaScoreRequest;
import com.example.solidconnection.score.dto.GpaScoreStatus;
import com.example.solidconnection.score.dto.GpaScoreStatusResponse;
import com.example.solidconnection.score.dto.LanguageTestScoreRequest;
import com.example.solidconnection.score.dto.LanguageTestScoreStatus;
import com.example.solidconnection.score.dto.LanguageTestScoreStatusResponse;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.ImgType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
        newGpaScore.setSiteUser(siteUser);
        GpaScore savedNewGpaScore = gpaScoreRepository.save(newGpaScore);  // 저장 후 반환된 객체
        return savedNewGpaScore.getId();  // 저장된 GPA Score의 ID 반환
    }

    @Transactional
    public Long submitLanguageTestScore(SiteUser siteUser, LanguageTestScoreRequest languageTestScoreRequest, MultipartFile file) {
        UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(file, ImgType.LANGUAGE_TEST);
        LanguageTest languageTest = new LanguageTest(languageTestScoreRequest.languageTestType(),
                languageTestScoreRequest.languageTestScore(), uploadedFile.fileUrl());
        LanguageTestScore newScore = new LanguageTestScore(languageTest, siteUser);
        newScore.setSiteUser(siteUser);
        LanguageTestScore savedNewScore = languageTestScoreRepository.save(newScore);  // 새로 저장한 객체
        return savedNewScore.getId();  // 저장된 객체의 ID 반환
    }

    @Transactional(readOnly = true)
    public GpaScoreStatusResponse getGpaScoreStatus(SiteUser siteUser) {
        List<GpaScoreStatus> gpaScoreStatusList =
                Optional.ofNullable(siteUser.getGpaScoreList())
                        .map(scores -> scores.stream()
                                .map(GpaScoreStatus::from)
                                .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());
        return new GpaScoreStatusResponse(gpaScoreStatusList);
    }

    @Transactional(readOnly = true)
    public LanguageTestScoreStatusResponse getLanguageTestScoreStatus(SiteUser siteUser) {
        List<LanguageTestScoreStatus> languageTestScoreStatusList =
                Optional.ofNullable(siteUser.getLanguageTestScoreList())
                        .map(scores -> scores.stream()
                                .map(LanguageTestScoreStatus::from)
                                .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());
        return new LanguageTestScoreStatusResponse(languageTestScoreStatusList);
    }
}
