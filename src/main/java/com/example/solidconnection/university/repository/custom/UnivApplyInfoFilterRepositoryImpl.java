package com.example.solidconnection.university.repository.custom;

import com.example.solidconnection.location.country.domain.QCountry;
import com.example.solidconnection.location.region.domain.QRegion;
import com.example.solidconnection.university.domain.QHomeUniversity;
import com.example.solidconnection.university.domain.QHostUniversity;
import com.example.solidconnection.university.domain.QLanguageRequirement;
import com.example.solidconnection.university.domain.QUnivApplyInfo;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
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
    public List<UnivApplyInfo> findAllByRegionCodeAndKeywordsAndTermIdAndHomeUniversityId(
            String regionCode,
            List<String> keywords,
            Long termId,
            long homeUniversityId
    ) {
        QUnivApplyInfo univApplyInfo = QUnivApplyInfo.univApplyInfo;
        QHostUniversity university = QHostUniversity.hostUniversity;
        QHomeUniversity homeUniversity = QHomeUniversity.homeUniversity;
        QCountry country = QCountry.country;
        QLanguageRequirement languageRequirement = QLanguageRequirement.languageRequirement;

        return queryFactory
                .selectFrom(univApplyInfo)
                .join(univApplyInfo.university, university).fetchJoin()
                .join(university.country, country).fetchJoin()
                .join(univApplyInfo.homeUniversity, homeUniversity).fetchJoin()
                .leftJoin(univApplyInfo.languageRequirements, languageRequirement).fetchJoin()
                .where(
                        regionCodeEq(country, regionCode)
                                .and(countryOrUniversityContainsKeyword(country, university, keywords))
                                .and(univApplyInfo.termId.eq(termId))
                                .and(homeUniversity.id.eq(homeUniversityId))
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

    private BooleanExpression countryOrUniversityContainsKeyword(QCountry country, QHostUniversity university, List<String> keywords) {
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

    private BooleanExpression termIdEq(QUnivApplyInfo univApplyInfo, Long givenTermId) {
        if (givenTermId == null) {
            return null;
        }
        return univApplyInfo.termId.eq(givenTermId);
    }

    // 1차 쿼리 개선(2026-09-21, 미커밋 로컬 검증용):
    // languageRequirements(1:N)를 fetchJoin으로 같이 가져오면 uia 1건당 최소 2행으로 fan-out되는데,
    // 이 메서드는 .distinct()도 없어서 결과 List<UnivApplyInfo>에 같은 uia가 중복 원소로 들어가는
    // 정합성 문제까지 있었다(EXPLAIN 상으로도 uia 6,019건인데 조인 결과가 12,875행으로 늘어나는 것으로 확인).
    // languageRequirements는 필터/정렬에 쓰이지 않으므로 메인 쿼리에서 fetchJoin을 제거하고,
    // UnivApplyInfo.languageRequirements에 @BatchSize를 추가해 필요한 시점에 IN절 배치 쿼리로 지연 로딩되게 했다.
    @Override
    public List<UnivApplyInfo> findAllByText(String text, Long termId, Long homeUniversityId) {
        QUnivApplyInfo univApplyInfo = QUnivApplyInfo.univApplyInfo;
        QHostUniversity university = QHostUniversity.hostUniversity;
        QHomeUniversity homeUniversity = QHomeUniversity.homeUniversity;
        QCountry country = QCountry.country;
        QRegion region = QRegion.region;

        JPAQuery<UnivApplyInfo> base = queryFactory.selectFrom(univApplyInfo)
                .join(univApplyInfo.university, university).fetchJoin()
                .join(university.country, country).fetchJoin()
                .join(region).on(country.regionCode.eq(region.code))
                .leftJoin(univApplyInfo.homeUniversity, homeUniversity).fetchJoin()
                .where(
                        termIdEq(univApplyInfo, termId),
                        homeUniversityIdEq(homeUniversity, homeUniversityId)
                );

        // text 가 비어있다면 모든 대학 지원 정보를 id 오름차순으로 정렬하여 반환
        if (text == null || text.isBlank()) {
            return base.orderBy(univApplyInfo.id.asc()).fetch();
        }

        // 매칭 조건 (대학 지원 정보명/국가명/지역명 중 하나라도 포함)
        BooleanExpression univApplyInfoLike = univApplyInfo.koreanName.contains(text);
        BooleanExpression countryLike = country.koreanName.contains(text);
        BooleanExpression regionLike = region.koreanName.contains(text);
        BooleanBuilder where = new BooleanBuilder()
                .or(univApplyInfoLike)
                .or(countryLike)
                .or(regionLike);

        // 우선순위 랭크: 대학 지원 정보명(0) > 국가명(1) > 지역명(2) > 그 외(3)
        NumberExpression<Integer> rank = new CaseBuilder()
                .when(univApplyInfoLike).then(0)
                .when(countryLike).then(1)
                .when(regionLike).then(2)
                .otherwise(3);

        // 정렬 조건: 랭크 오름차순 > 대학지원정보 id 오름차순
        return base.where(where)
                .orderBy(rank.asc(), univApplyInfo.id.asc())
                .fetch();
    }

    private BooleanExpression homeUniversityIdEq(QHomeUniversity homeUniversity, Long homeUniversityId) {
        if (homeUniversityId == null) {
            return null;
        }
        return homeUniversity.id.eq(homeUniversityId);
    }
}
