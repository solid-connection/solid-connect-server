package com.example.solidconnection.university.repository.custom;

import com.example.solidconnection.location.country.domain.QCountry;
import com.example.solidconnection.location.region.domain.QRegion;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.QUnivApplyInfo;
import com.example.solidconnection.university.domain.QUniversity;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.domain.QLanguageRequirement;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

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
        QRegion region = QRegion.region;
        QLanguageRequirement languageRequirement = QLanguageRequirement.languageRequirement;

        return queryFactory
                .selectFrom(univApplyInfo)
                .join(univApplyInfo.university, university).fetchJoin()
                .join(university.country, country).fetchJoin()
                .join(country.region, region).fetchJoin()
                .leftJoin(univApplyInfo.languageRequirements, languageRequirement).fetchJoin()
                .where(
                        regionCodeEq(region, regionCode)
                                .and(countryOrUniversityContainsKeyword(country, university, keywords))
                )
                .distinct()
                .fetch();
    }


    private BooleanExpression regionCodeEq(QRegion region, String regionCode) {
        if (regionCode == null || regionCode.isEmpty()) {
            return Expressions.asBoolean(true).isTrue();
        }
        return region.code.eq(regionCode);
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
    public List<UnivApplyInfo> findAllByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScoreAndTerm(
            String regionCode, List<String> keywords, LanguageTestType testType, String testScore, String term) {
        QUniversity university = QUniversity.university;
        QCountry country = QCountry.country;
        QRegion region = QRegion.region;
        QUnivApplyInfo univApplyInfo = QUnivApplyInfo.univApplyInfo;

        List<UnivApplyInfo> filteredUnivApplyInfo = queryFactory
                .selectFrom(univApplyInfo)
                .join(univApplyInfo.university, university)
                .join(university.country, country)
                .join(university.region, region)
                .where(regionCodeEq(region, regionCode)
                        .and(countryOrUniversityContainsKeyword(country, university, keywords))
                        .and(univApplyInfo.term.eq(term)))
                .fetch();

        if (testScore == null || testScore.isEmpty()) {
            if (testType != null) {
                return filteredUnivApplyInfo.stream()
                        .filter(uai -> uai.getLanguageRequirements().stream()
                                .anyMatch(lr -> lr.getLanguageTestType().equals(testType)))
                        .toList();
            }
            return filteredUnivApplyInfo;
        }

        return filteredUnivApplyInfo.stream()
                .filter(uai -> compareMyTestScoreToMinPassScore(uai, testType, testScore) >= 0)
                .toList();
    }

    private int compareMyTestScoreToMinPassScore(UnivApplyInfo univApplyInfo, LanguageTestType testType, String testScore) {
        return univApplyInfo.getLanguageRequirements().stream()
                .filter(languageRequirement -> languageRequirement.getLanguageTestType().equals(testType))
                .findFirst()
                .map(requirement -> testType.compare(testScore, requirement.getMinScore()))
                .orElse(-1);
    }
}
