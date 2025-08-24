package com.example.solidconnection.university.repository.custom;

import com.example.solidconnection.location.country.domain.QCountry;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.QLanguageRequirement;
import com.example.solidconnection.university.domain.QUnivApplyInfo;
import com.example.solidconnection.university.domain.QUniversity;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UnivApplyInfoFilterRepositoryImpl implements UnivApplyInfoFilterRepository {

    private final JPAQueryFactory queryFactory;

    @Autowired
    public UnivApplyInfoFilterRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<UnivApplyInfo> findAllByRegionCodeAndKeywords(String regionCode, List<String> keywords) {
        QUnivApplyInfo univApplyInfo = QUnivApplyInfo.univApplyInfo;
        QUniversity university = QUniversity.university;
        QCountry country = QCountry.country;
        QLanguageRequirement languageRequirement = QLanguageRequirement.languageRequirement;

        return queryFactory
                .selectFrom(univApplyInfo)
                .join(univApplyInfo.university, university).fetchJoin()
                .join(university.country, country).fetchJoin()
                .leftJoin(univApplyInfo.languageRequirements, languageRequirement).fetchJoin()
                .where(
                        regionCodeEq(country, regionCode)
                                .and(countryOrUniversityContainsKeyword(country, university, keywords))
                )
                .distinct()
                .fetch();
    }

    private BooleanExpression regionCodeEq(QCountry country, String regionCode) {
        if (regionCode == null || regionCode.isEmpty()) {
            return Expressions.asBoolean(true).isTrue();
        }
        return country.regionCode.eq(regionCode);
    }

    private BooleanExpression countryOrUniversityContainsKeyword(QCountry country, QUniversity university, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return Expressions.TRUE;
        }
        BooleanExpression countryCondition = createKeywordCondition(country.koreanName, keywords);
        BooleanExpression universityCondition = createKeywordCondition(university.koreanName, keywords);
        return countryCondition.or(universityCondition);
    }

    private BooleanExpression createKeywordCondition(StringPath namePath, List<String> keywords) {
        return keywords.stream()
                .map(namePath::contains)
                .reduce(BooleanExpression::or)
                .orElse(Expressions.FALSE);
    }

    @Override
    public List<UnivApplyInfo> findAllByFilter(
            LanguageTestType testType, String testScore, String term, List<String> countryCodes
    ) {
        QUniversity university = QUniversity.university;
        QUnivApplyInfo univApplyInfo = QUnivApplyInfo.univApplyInfo;
        QCountry country = QCountry.country;
        QLanguageRequirement languageRequirement = QLanguageRequirement.languageRequirement;

        List<UnivApplyInfo> filteredUnivApplyInfo = queryFactory.selectFrom(univApplyInfo)
                .join(univApplyInfo.university, university)
                .join(university.country, country)
                .join(univApplyInfo.languageRequirements, languageRequirement)
                .fetchJoin()
                .where(
                        languageTestTypeEq(languageRequirement, testType),
                        termEq(univApplyInfo, term),
                        countryCodesIn(country, countryCodes)
                )
                .distinct()
                .fetch();

        if (testScore == null || testScore.isBlank()) {
            return filteredUnivApplyInfo;
        }

        /*
         * 시험 유형에 따라 성적 비교 방식이 다르다.
         * 입력된 점수가 대학에서 요구하는 최소 점수보다 높은지를 '쿼리로' 비교하기엔 쿼리가 지나치게 복잡해진다.
         * 따라서 이 부분만 자바 코드로 필터링한다.
         * */
        return filteredUnivApplyInfo.stream()
                .filter(uai -> isGivenScoreOverMinPassScore(uai, testType, testScore))
                .toList();
    }

    private BooleanExpression languageTestTypeEq(
            QLanguageRequirement languageRequirement, LanguageTestType givenTestType
    ) {
        if (givenTestType == null) {
            return null;
        }
        return languageRequirement.languageTestType.eq(givenTestType);
    }

    private BooleanExpression termEq(QUnivApplyInfo univApplyInfo, String givenTerm) {
        if (givenTerm == null || givenTerm.isBlank()) {
            return null;
        }
        return univApplyInfo.term.eq(givenTerm);
    }

    private BooleanExpression countryCodesIn(QCountry country, List<String> givenCountryCodes) {
        if (givenCountryCodes == null || givenCountryCodes.isEmpty()) {
            return null;
        }
        return country.code.in(givenCountryCodes);
    }

    private boolean isGivenScoreOverMinPassScore(
            UnivApplyInfo univApplyInfo, LanguageTestType givenTestType, String givenTestScore
    ) {
        return univApplyInfo.getLanguageRequirements().stream()
                .filter(languageRequirement -> languageRequirement.getLanguageTestType().equals(givenTestType))
                .findFirst()
                .map(requirement -> givenTestType.compare(givenTestScore, requirement.getMinScore()))
                .orElse(-1) >= 0;
    }
}
