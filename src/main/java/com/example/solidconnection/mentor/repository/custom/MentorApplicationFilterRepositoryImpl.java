package com.example.solidconnection.mentor.repository.custom;

import static com.example.solidconnection.location.country.domain.QCountry.country;
import static com.example.solidconnection.location.region.domain.QRegion.region;
import static com.example.solidconnection.mentor.domain.QMentorApplication.mentorApplication;
import static com.example.solidconnection.siteuser.domain.QSiteUser.siteUser;
import static com.example.solidconnection.university.domain.QUniversity.university;
import static org.springframework.util.StringUtils.hasText;

import com.example.solidconnection.admin.dto.MentorApplicationResponse;
import com.example.solidconnection.admin.dto.MentorApplicationSearchCondition;
import com.example.solidconnection.admin.dto.MentorApplicationSearchResponse;
import com.example.solidconnection.admin.dto.SiteUserResponse;
import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import com.example.solidconnection.mentor.domain.UniversitySelectType;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class MentorApplicationFilterRepositoryImpl implements MentorApplicationFilterRepository {

    private static final ZoneId SYSTEM_ZONE_ID = ZoneId.systemDefault();

    private static final ConstructorExpression<SiteUserResponse> SITE_USER_RESPONSE_PROJECTION =
            Projections.constructor(
                    SiteUserResponse.class,
                    siteUser.id,
                    siteUser.nickname,
                    siteUser.profileImageUrl
            );

    private static final ConstructorExpression<MentorApplicationResponse> MENTOR_APPLICATION_RESPONSE_PROJECTION =
            Projections.constructor(
                    MentorApplicationResponse.class,
                    mentorApplication.id,
                    region.koreanName,
                    country.koreanName,
                    university.koreanName,
                    mentorApplication.mentorProofUrl,
                    mentorApplication.mentorApplicationStatus,
                    mentorApplication.rejectedReason,
                    mentorApplication.createdAt,
                    mentorApplication.approvedAt
            );

    private static final ConstructorExpression<MentorApplicationSearchResponse> MENTOR_APPLICATION_SEARCH_RESPONSE_PROJECTION =
            Projections.constructor(
                    MentorApplicationSearchResponse.class,
                    SITE_USER_RESPONSE_PROJECTION,
                    MENTOR_APPLICATION_RESPONSE_PROJECTION
            );

    private final JPAQueryFactory queryFactory;

    @Autowired
    public MentorApplicationFilterRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<MentorApplicationSearchResponse> searchMentorApplications(MentorApplicationSearchCondition condition, Pageable pageable) {
        List<MentorApplicationSearchResponse> content = queryFactory
                .select(MENTOR_APPLICATION_SEARCH_RESPONSE_PROJECTION)
                .from(mentorApplication)
                .join(siteUser).on(mentorApplication.siteUserId.eq(siteUser.id))
                .leftJoin(university).on(mentorApplication.universityId.eq(university.id))
                .leftJoin(region).on(university.region.eq(region))
                .leftJoin(country).on(university.country.eq(country))
                .where(
                        verifyMentorStatusEq(condition.mentorApplicationStatus()),
                        keywordContains(condition.keyword()),
                        createdAtEq(condition.createdAt()),
                        universitySelectTypeEq(condition.universitySelectType())
                )
                .orderBy(mentorApplication.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = createCountQuery(condition).fetchOne();

        return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
    }

    private JPAQuery<Long> createCountQuery(MentorApplicationSearchCondition condition) {
        JPAQuery<Long> query = queryFactory
                .select(mentorApplication.count())
                .from(mentorApplication);

        String keyword = condition.keyword();

        if (hasText(keyword)) {
            query.join(siteUser).on(mentorApplication.siteUserId.eq(siteUser.id))
                    .leftJoin(university).on(mentorApplication.universityId.eq(university.id))
                    .leftJoin(region).on(university.region.eq(region))
                    .leftJoin(country).on(university.country.eq(country));
        }

        return query.where(
                verifyMentorStatusEq(condition.mentorApplicationStatus()),
                keywordContains(condition.keyword()),
                createdAtEq(condition.createdAt()),
                universitySelectTypeEq(condition.universitySelectType())
        );
    }

    private BooleanExpression verifyMentorStatusEq(MentorApplicationStatus status) {
        return status != null ? mentorApplication.mentorApplicationStatus.eq(status) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!hasText(keyword)) {
            return null;
        }

        return siteUser.nickname.containsIgnoreCase(keyword)
                .or(university.koreanName.containsIgnoreCase(keyword))
                .or(region.koreanName.containsIgnoreCase(keyword))
                .or(country.koreanName.containsIgnoreCase(keyword));
    }

    private BooleanExpression createdAtEq(LocalDate createdAt) {
        if (createdAt == null) {
            return null;
        }

        LocalDateTime startOfDay = createdAt.atStartOfDay();
        LocalDateTime endOfDay = createdAt.plusDays(1).atStartOfDay().minusNanos(1);

        return mentorApplication.createdAt.between(
                startOfDay.atZone(SYSTEM_ZONE_ID),
                endOfDay.atZone(SYSTEM_ZONE_ID)
        );
    }

    private BooleanExpression universitySelectTypeEq(UniversitySelectType universitySelectType) {
        return universitySelectType != null ? mentorApplication.universitySelectType.eq(universitySelectType) : null;
    }
}
