package com.example.solidconnection.score.service;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.custom.exception.CustomException;
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
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.ImgType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.solidconnection.custom.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final GpaScoreRepository gpaScoreRepository;
    private final S3Service s3Service;
    private final LanguageTestScoreRepository languageTestScoreRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional
    public Long submitGpaScore(SiteUser siteUser, GpaScoreRequest gpaScoreRequest, MultipartFile file) {
        UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(file, ImgType.GPA);
        Gpa gpa = new Gpa(gpaScoreRequest.gpa(), gpaScoreRequest.gpaCriteria(), uploadedFile.fileUrl());

        /*
         * todo: siteUser를 영속 상태로 만들 수 있도록 컨트롤러에서 siteUserId 를 넘겨줄 것인지,
         *  siteUser 에 gpaScoreList 를 FetchType.EAGER 로 설정할 것인지,
         *  gpa 와 siteUser 사이의 양방향을 끊을 것인지 생각해봐야한다.
         */
        SiteUser siteUser1 = siteUserRepository.findById(siteUser.getId()).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        GpaScore newGpaScore = new GpaScore(gpa, siteUser1);
        newGpaScore.setSiteUser(siteUser1);
        GpaScore savedNewGpaScore = gpaScoreRepository.save(newGpaScore);  // 저장 후 반환된 객체
        return savedNewGpaScore.getId();  // 저장된 GPA Score의 ID 반환
    }

    @Transactional
    public Long submitLanguageTestScore(SiteUser siteUser, LanguageTestScoreRequest languageTestScoreRequest, MultipartFile file) {
        UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(file, ImgType.LANGUAGE_TEST);
        LanguageTest languageTest = new LanguageTest(languageTestScoreRequest.languageTestType(),
                languageTestScoreRequest.languageTestScore(), uploadedFile.fileUrl());

        /*
         * todo: siteUser를 영속 상태로 만들 수 있도록 컨트롤러에서 siteUserId 를 넘겨줄 것인지,
         *  siteUser 에 languageTestScoreList 를 FetchType.EAGER 로 설정할 것인지,
         *  languageTest 와 siteUser 사이의 양방향을 끊을 것인지 생각해봐야한다.
         */
        SiteUser siteUser1 = siteUserRepository.findById(siteUser.getId()).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        LanguageTestScore newScore = new LanguageTestScore(languageTest, siteUser1);
        newScore.setSiteUser(siteUser1);
        LanguageTestScore savedNewScore = languageTestScoreRepository.save(newScore);  // 새로 저장한 객체
        return savedNewScore.getId();  // 저장된 객체의 ID 반환
    }

    @Transactional(readOnly = true)
    public GpaScoreStatusResponse getGpaScoreStatus(SiteUser siteUser) {
        // todo: ditto
        SiteUser siteUser1 = siteUserRepository.findById(siteUser.getId()).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        List<GpaScoreStatus> gpaScoreStatusList =
                Optional.ofNullable(siteUser1.getGpaScoreList())
                        .map(scores -> scores.stream()
                                .map(GpaScoreStatus::from)
                                .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());
        return new GpaScoreStatusResponse(gpaScoreStatusList);
    }

    @Transactional(readOnly = true)
    public LanguageTestScoreStatusResponse getLanguageTestScoreStatus(SiteUser siteUser) {
        // todo: ditto
        SiteUser siteUser1 = siteUserRepository.findById(siteUser.getId()).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        List<LanguageTestScoreStatus> languageTestScoreStatusList =
                Optional.ofNullable(siteUser1.getLanguageTestScoreList())
                        .map(scores -> scores.stream()
                                .map(LanguageTestScoreStatus::from)
                                .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());
        return new LanguageTestScoreStatusResponse(languageTestScoreStatusList);
    }
}
