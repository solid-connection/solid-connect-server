package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.dto.ScoreRequestDto;
import com.example.solidconnection.application.dto.UniversityRequestDto;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.entity.UniversityInfoForApply;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.service.UniversityValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.example.solidconnection.constants.Constants.TERM;
import static com.example.solidconnection.custom.exception.ErrorCode.APPLY_UPDATE_LIMIT_EXCEED;
import static com.example.solidconnection.custom.exception.ErrorCode.CANT_APPLY_FOR_SAME_UNIVERSITY;

@RequiredArgsConstructor
@Transactional
@Service
public class ApplicationSubmissionService {

    private static final int APPLICATION_UPDATE_COUNT_LIMIT = 3;

    private final ApplicationRepository applicationRepository;
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final UniversityValidator universityValidator;
    private final SiteUserRepository siteUserRepository;

    /*
    * 학점과 영어 성적을 제출한다.
    * - 기존에 제출한 적이 있다면, 수정한다.
    * - 처음 제출한다면, 랜덤한 '제출 닉네임'을 부여하고 DB 에 저장한다.
    * */
    public boolean submitScore(String email, ScoreRequestDto scoreRequestDto) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        Gpa gpa = scoreRequestDto.toGpa();
        LanguageTest languageTest = scoreRequestDto.toLanguageTest();

        applicationRepository.findBySiteUser_Email(email)
                .ifPresentOrElse(
                        // 수정
                        application -> application.updateGpaAndLanguageTest(gpa, languageTest),

                        // 최초 등록
                        () -> applicationRepository.save(
                                new Application(siteUser, gpa, languageTest, getRandomNickname())
                        )
                );
        return true;
    }

    /*
     * 지망 대학교를 제출한다.
     * - 첫번째 지망과 두번째 지망이 같은지 검증한다.
     * - 기존에 제출한 적이 있다면, 수정한다.
     *   - 수정 횟수 제한을 초과하지 않았는지 검증한다.
     *   - 그리고 새로운 '제출 닉네임'을 부여한다. (악의적으로 타인의 변경 기록을 추적하는 것을 막기 위해)
     * - 처음 제출한다면, 랜덤한 '제출 닉네임'을 부여하고 DB 에 저장한다.
     * */
    public boolean submitUniversityChoice(String email, UniversityRequestDto universityRequestDto) {
        validateFirstAndSecondChoiceIdDifferent(universityRequestDto);

        SiteUser siteUser = siteUserRepository.getByEmail(email);
        UniversityInfoForApply firstChoiceUniversity = universityValidator.getValidatedUniversityInfoForApplyByIdAndTerm(universityRequestDto.getFirstChoiceUniversityId());
        UniversityInfoForApply secondChoiceUniversity = universityInfoForApplyRepository.findByIdAndTerm(universityRequestDto.getSecondChoiceUniversityId(), TERM).orElse(null);

        applicationRepository.findBySiteUser_Email(email)
                .ifPresentOrElse(
                        // 수정
                        application -> {
                            validateUpdateLimitNotExceed(application);
                            application.updateUniversityChoice(firstChoiceUniversity, secondChoiceUniversity, getRandomNickname());
                        },

                        // 최초 등록
                        () -> applicationRepository.save(
                                new Application(siteUser, firstChoiceUniversity, secondChoiceUniversity, getRandomNickname())
                        )
                );

        return true;
    }

    private String getRandomNickname() {
        String randomNickname = NicknameCreator.createRandomNickname();
        while (applicationRepository.existsByNicknameForApply(randomNickname)) {
            randomNickname = NicknameCreator.createRandomNickname();
        }
        return randomNickname;
    }

    private void validateUpdateLimitNotExceed(Application application) {
        if (application.getUpdateCount() > APPLICATION_UPDATE_COUNT_LIMIT) {
            throw new CustomException(APPLY_UPDATE_LIMIT_EXCEED);
        }
    }

    private void validateFirstAndSecondChoiceIdDifferent(UniversityRequestDto universityRequestDto) {
        if (Objects.equals(
                universityRequestDto.getFirstChoiceUniversityId(),
                universityRequestDto.getSecondChoiceUniversityId())) {
            throw new CustomException(CANT_APPLY_FOR_SAME_UNIVERSITY);
        }
    }
}
