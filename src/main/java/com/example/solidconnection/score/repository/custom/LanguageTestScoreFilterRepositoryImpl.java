package com.example.solidconnection.score.repository.custom;

import com.example.solidconnection.admin.dto.LanguageTestResponse;
import com.example.solidconnection.admin.dto.LanguageTestScoreSearchResponse;
import com.example.solidconnection.admin.dto.LanguageTestScoreStatusResponse;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.admin.dto.SiteUserResponse;
import com.example.solidconnection.type.VerifyStatus;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.example.solidconnection.score.domain.QLanguageTestScore.languageTestScore;
import static com.example.solidconnection.siteuser.domain.QSiteUser.siteUser;
import static io.jsonwebtoken.lang.Strings.hasText;

@Repository
public class LanguageTestScoreFilterRepositoryImpl implements LanguageTestScoreFilterRepository {

    private static final ZoneId SYSTEM_ZONE_ID = ZoneId.systemDefault();

    private static final ConstructorExpression<LanguageTestResponse> LANGUAGE_TEST_RESPONSE_PROJECTION = Projections.constructor(
            LanguageTestResponse.class,
            languageTestScore.languageTest.languageTestType,
            languageTestScore.languageTest.languageTestScore,
            languageTestScore.languageTest.languageTestReportUrl
    );
    private static final ConstructorExpression<LanguageTestScoreStatusResponse> LANGUAGE_TEST_SCORE_STATUS_RESPONSE_PROJECTION = Projections.constructor(
            LanguageTestScoreStatusResponse.class,
            languageTestScore.id,
            LANGUAGE_TEST_RESPONSE_PROJECTION,
            languageTestScore.verifyStatus,
            languageTestScore.rejectedReason,
            languageTestScore.createdAt,
            languageTestScore.updatedAt
    );
    private static final ConstructorExpression<SiteUserResponse> SITE_USER_RESPONSE_PROJECTION = Projections.constructor(
            SiteUserResponse.class,
            siteUser.id,
            siteUser.nickname,
            siteUser.profileImageUrl
    );
    private static final ConstructorExpression<LanguageTestScoreSearchResponse> LANGUAGE_TEST_SCORE_SEARCH_RESPONSE_PROJECTION = Projections.constructor(
            LanguageTestScoreSearchResponse.class,
            LANGUAGE_TEST_SCORE_STATUS_RESPONSE_PROJECTION,
            SITE_USER_RESPONSE_PROJECTION
    );

    private final JPAQueryFactory queryFactory;

    @Autowired
    public LanguageTestScoreFilterRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<LanguageTestScoreSearchResponse> searchLanguageTestScores(ScoreSearchCondition condition, Pageable pageable) {
        List<LanguageTestScoreSearchResponse> content = queryFactory
                .select(LANGUAGE_TEST_SCORE_SEARCH_RESPONSE_PROJECTION)
                .from(languageTestScore)
                .join(languageTestScore.siteUser, siteUser)
                .where(
                        verifyStatusEq(condition.verifyStatus()),
                        nicknameContains(condition.nickname()),
                        createdAtEq(condition.createdAt())
                )
                .orderBy(languageTestScore.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(languageTestScore.count())
                .from(languageTestScore)
                .join(languageTestScore.siteUser, siteUser)
                .where(
                        verifyStatusEq(condition.verifyStatus()),
                        nicknameContains(condition.nickname()),
                        createdAtEq(condition.createdAt())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
    }

    private BooleanExpression verifyStatusEq(VerifyStatus verifyStatus) {
        return verifyStatus != null ? languageTestScore.verifyStatus.eq(verifyStatus) : null;
    }

    private BooleanExpression nicknameContains(String nickname) {
        return hasText(nickname) ? siteUser.nickname.contains(nickname) : null;
    }

    private BooleanExpression createdAtEq(LocalDate createdAt) {
        if (createdAt == null) {
            return null;
        }

        LocalDateTime startOfDay = createdAt.atStartOfDay();
        LocalDateTime endOfDay = createdAt.plusDays(1).atStartOfDay().minusNanos(1);

        return languageTestScore.createdAt.between(
                startOfDay.atZone(SYSTEM_ZONE_ID),
                endOfDay.atZone(SYSTEM_ZONE_ID)
        );
    }
}
