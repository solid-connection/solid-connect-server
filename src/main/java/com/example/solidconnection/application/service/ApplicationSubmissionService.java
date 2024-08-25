package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.dto.ScoreRequest;
import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.example.solidconnection.custom.exception.ErrorCode.*;

@RequiredArgsConstructor
@Service
public class ApplicationSubmissionService {

    public static final int APPLICATION_UPDATE_COUNT_LIMIT = 3;

    private final ApplicationRepository applicationRepository;
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final SiteUserRepository siteUserRepository;

    @Value("${university.term}")
    private String term;

    /*
     * 학점과 영어 성적을 제출한다.
     * - 금학기에 제출한 적이 있다면, 수정한다.
     * - 성적을 제출한적이 한번도 없거나 제출한적이 있지만 금학기에 제출한 적이 없다면 새로 등록한다.
     * - 수정을 하고 나면, 성적 승인 상태(verifyStatus)를 PENDING 상태로 변경한다.
     * */
    @Transactional
    public boolean submitScore(String email, ScoreRequest scoreRequest) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        Gpa gpa = scoreRequest.toGpa();
        LanguageTest languageTest = scoreRequest.toLanguageTest();

        applicationRepository.findBySiteUserAndTerm(siteUser, term)
                .ifPresentOrElse(
                        // 금학기에 성적 제출 이력이 있는 경우
                        application -> application.updateGpaAndLanguageTest(gpa, languageTest),
                        () -> {
                            // 성적 제출한적이 한번도 없는 경우 && 성적 제출한적이 있지만 금학기에 없는 경우
                            applicationRepository.save(new Application(siteUser, gpa, languageTest, term));
                        }
                );
        return true;
    }

    /*
     * 지망 대학교를 제출한다.
     * - 지망 대학중 중복된 대학교가 있는지 검증한다.
     * - 지원 정보 제출 내역이 없다면, 지금의 프로세스(성적 제출 후 지망대학 제출)에 벗어나는 요청이므로 예외를 응답한다.
     * - 기존에 제출한 적이 있다면, 수정한다.
     *   - 수정 횟수 제한을 초과하지 않았는지 검증한다.
     *   - 새로운 '제출 닉네임'을 부여한다. (악의적으로 타인의 변경 기록을 추적하는 것을 막기 위해)
     *   - 성적 승인 상태(verifyStatus) 는 변경하지 않는다.
     * */
    @Transactional
    public boolean submitUniversityChoice(String email, UniversityChoiceRequest universityChoiceRequest) {
        validateNoDuplicateUniversityChoices(universityChoiceRequest);

        // 성적 제출한 적이 한번도 없는 경우
        Application existingApplication = applicationRepository.findTop1BySiteUser_EmailOrderByTermDesc(email)
                .orElseThrow(() -> new CustomException(SCORE_SHOULD_SUBMITTED_FIRST));

        Application application = Optional.of(existingApplication)
                .filter(app -> !app.getTerm().equals(term))
                .map(app -> {
                    // 성적 제출한 적이 있지만 금학기에 없는 경우, 이전 성적으로 새 Application 객체를 등록
                    SiteUser siteUser = siteUserRepository.getByEmail(email);
                    return applicationRepository.save(new Application(siteUser, app.getGpa(), app.getLanguageTest(), term));
                })
                .orElse(existingApplication); // 금학기에 이미 성적 제출한 경우 기존 객체 사용

        UniversityInfoForApply firstChoiceUniversity = universityInfoForApplyRepository
                .getUniversityInfoForApplyByIdAndTerm(universityChoiceRequest.firstChoiceUniversityId(), term);
        UniversityInfoForApply secondChoiceUniversity = universityInfoForApplyRepository
                .getUniversityInfoForApplyByIdAndTerm(universityChoiceRequest.secondChoiceUniversityId(), term);
        UniversityInfoForApply thirdChoiceUniversity = universityInfoForApplyRepository
                .getUniversityInfoForApplyByIdAndTerm(universityChoiceRequest.thirdChoiceUniversityId(), term);

        validateUpdateLimitNotExceed(application);
        application.updateUniversityChoice(firstChoiceUniversity, secondChoiceUniversity, thirdChoiceUniversity, getRandomNickname());
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
        if (application.getUpdateCount() >= APPLICATION_UPDATE_COUNT_LIMIT) {
            throw new CustomException(APPLY_UPDATE_LIMIT_EXCEED);
        }
    }

    private void validateNoDuplicateUniversityChoices(UniversityChoiceRequest universityChoiceRequest) {
        Set<Long> uniqueUniversityIds = new HashSet<>();

        uniqueUniversityIds.add(universityChoiceRequest.firstChoiceUniversityId());
        uniqueUniversityIds.add(universityChoiceRequest.secondChoiceUniversityId());
        uniqueUniversityIds.add(universityChoiceRequest.thirdChoiceUniversityId());

        if (uniqueUniversityIds.size() < 3) {
            throw new CustomException(CANT_APPLY_FOR_SAME_UNIVERSITY);
        }
    }
}
