package com.example.solidconnection.score.repository.custom;

import com.example.solidconnection.admin.dto.GpaScoreSearchResponse;
import com.example.solidconnection.admin.dto.GpaScoreStatus;
import com.example.solidconnection.admin.dto.ScoreSearchCondition;
import com.example.solidconnection.admin.dto.SiteUserDto;
import com.example.solidconnection.type.VerifyStatus;
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

import static com.example.solidconnection.score.domain.QGpaScore.gpaScore;
import static com.example.solidconnection.siteuser.domain.QSiteUser.siteUser;
import static org.springframework.util.StringUtils.hasText;

@Repository
public class GpaScoreFilterRepositoryImpl implements GpaScoreFilterRepository {
    private final JPAQueryFactory queryFactory;

    @Autowired
    public GpaScoreFilterRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<GpaScoreSearchResponse> searchGpaScores(ScoreSearchCondition condition, Pageable pageable) {
        List<GpaScoreSearchResponse> content = queryFactory
                .select(Projections.constructor(GpaScoreSearchResponse.class,
                        Projections.constructor(GpaScoreStatus.class,
                                gpaScore.id,
                                gpaScore.gpa,
                                gpaScore.verifyStatus,
                                gpaScore.rejectedReason,
                                gpaScore.createdAt,
                                gpaScore.updatedAt
                        ),
                        Projections.constructor(SiteUserDto.class,
                                siteUser.id,
                                siteUser.nickname,
                                siteUser.profileImageUrl
                        )
                ))
                .from(gpaScore)
                .join(gpaScore.siteUser, siteUser)
                .where(
                        verifyStatusEq(condition.verifyStatus()),
                        nicknameContains(condition.nickname()),
                        createdAtEq(condition.createdAt())
                )
                .orderBy(gpaScore.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(gpaScore.count())
                .from(gpaScore)
                .join(gpaScore.siteUser, siteUser)
                .where(
                        verifyStatusEq(condition.verifyStatus()),
                        nicknameContains(condition.nickname()),
                        createdAtEq(condition.createdAt())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
    }

    private BooleanExpression verifyStatusEq(VerifyStatus verifyStatus) {
        return verifyStatus != null ? gpaScore.verifyStatus.eq(verifyStatus) : null;
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

        return gpaScore.createdAt.between(
                startOfDay.atZone(ZoneId.systemDefault()),
                endOfDay.atZone(ZoneId.systemDefault())
        );
    }
}
