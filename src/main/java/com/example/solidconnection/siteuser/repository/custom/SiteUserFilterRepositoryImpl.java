package com.example.solidconnection.siteuser.repository.custom;

import static com.example.solidconnection.application.domain.QApplication.application;
import static com.example.solidconnection.mentor.domain.QMentor.mentor;
import static com.example.solidconnection.mentor.domain.QMentorApplication.mentorApplication;
import static com.example.solidconnection.mentor.domain.QMentoring.mentoring;
import static com.example.solidconnection.report.domain.QReport.report;
import static com.example.solidconnection.siteuser.domain.QSiteUser.siteUser;
import static com.example.solidconnection.siteuser.domain.QUserBan.userBan;
import static com.example.solidconnection.university.domain.QUnivApplyInfo.univApplyInfo;
import static org.springframework.util.StringUtils.hasText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.solidconnection.admin.dto.RestrictedUserInfoDetailResponse;
import com.example.solidconnection.university.domain.QUnivApplyInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.solidconnection.admin.dto.BannedHistoryResponse;
import com.example.solidconnection.admin.dto.BannedInfoResponse;
import com.example.solidconnection.admin.dto.MatchedInfoResponse;
import com.example.solidconnection.admin.dto.MenteeInfoResponse;
import com.example.solidconnection.admin.dto.MentorApplicationHistoryResponse;
import com.example.solidconnection.admin.dto.MentorInfoResponse;
import com.example.solidconnection.admin.dto.ReportedHistoryResponse;
import com.example.solidconnection.admin.dto.ReportedInfoResponse;
import com.example.solidconnection.admin.dto.RestrictedUserSearchCondition;
import com.example.solidconnection.admin.dto.RestrictedUserSearchResponse;
import com.example.solidconnection.admin.dto.UnivApplyInfoResponse;
import com.example.solidconnection.admin.dto.UserInfoDetailResponse;
import com.example.solidconnection.admin.dto.UserSearchCondition;
import com.example.solidconnection.admin.dto.UserSearchResponse;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.UserStatus;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@Repository
public class SiteUserFilterRepositoryImpl implements SiteUserFilterRepository {

    private static final ConstructorExpression<UserSearchResponse> USER_SEARCH_RESPONSE_PROJECTION =
            Projections.constructor(
                    UserSearchResponse.class,
                    siteUser.nickname,
                    siteUser.email,
                    siteUser.role,
                    siteUser.userStatus
            );

    private static final ConstructorExpression<ReportedInfoResponse> REPORTED_INFO_RESPONSE_PROJECTION =
            Projections.constructor(
                    ReportedInfoResponse.class,
                    report.createdAt,
                    report.targetType,
                    report.reportType
            );

    private static final ConstructorExpression<BannedInfoResponse> BANNED_INFO_RESPONSE_PROJECTION =
            Projections.constructor(
                    BannedInfoResponse.class,
                    siteUser.userStatus.eq(UserStatus.BANNED),
                    userBan.duration
            );

    private static final ConstructorExpression<RestrictedUserSearchResponse> RESTRICTED_USER_SEARCH_RESPONSE_PROJECTION =
            Projections.constructor(
                    RestrictedUserSearchResponse.class,
                    siteUser.nickname,
                    siteUser.role,
                    siteUser.userStatus,
                    REPORTED_INFO_RESPONSE_PROJECTION,
                    BANNED_INFO_RESPONSE_PROJECTION
            );

    private static final ConstructorExpression<ReportedHistoryResponse> REPORTED_HISTORY_RESPONSE_PROJECTION =
            Projections.constructor(
                    ReportedHistoryResponse.class,
                    report.createdAt,
                    report.reportType
            );

    private static final ConstructorExpression<MatchedInfoResponse> MATCHED_INFO_RESPONSE_PROJECTION =
            Projections.constructor(
                    MatchedInfoResponse.class,
                    siteUser.nickname,
                    mentoring.confirmedAt
            );

    private static final ConstructorExpression<MentorApplicationHistoryResponse> MENTOR_APPLICATION_HISTORY_RESPONSE_PROJECTION =
            Projections.constructor(
                    MentorApplicationHistoryResponse.class,
                    mentorApplication.mentorApplicationStatus,
                    mentorApplication.rejectedReason,
                    mentorApplication.createdAt
            );

    private static final ConstructorExpression<BannedHistoryResponse> BANNED_HISTORY_RESPONSE_PROJECTION =
            Projections.constructor(
                    BannedHistoryResponse.class,
                    userBan.createdAt
            );

    private static final QUnivApplyInfo firstChoiceUnivApplyInfo = new QUnivApplyInfo("firstChoiceUnivApplyInfo");
    private static final QUnivApplyInfo secondChoiceUnivApplyInfo = new QUnivApplyInfo("secondChoiceUnivApplyInfo");
    private static final QUnivApplyInfo thirdChoiceUnivApplyInfo = new QUnivApplyInfo("thirdChoiceUnivApplyInfo");

    private static final ConstructorExpression<UnivApplyInfoResponse> UNIV_APPLY_INFO_RESPONSE_PROJECTION =
            Projections.constructor(
                    UnivApplyInfoResponse.class,
                    firstChoiceUnivApplyInfo.koreanName,
                    secondChoiceUnivApplyInfo.koreanName,
                    thirdChoiceUnivApplyInfo.koreanName
            );

    private final JPAQueryFactory queryFactory;

    @Autowired
    public SiteUserFilterRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<UserSearchResponse> searchAllUsers(UserSearchCondition condition, Pageable pageable) {
        List<UserSearchResponse> content = queryFactory
                .select(USER_SEARCH_RESPONSE_PROJECTION)
                .from(siteUser)
                .where(
                        roleEq(condition.role()),
                        keywordContains(condition.keyword())
                )
                .orderBy(siteUser.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = createUserCountQuery(condition).fetchOne();
        return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
    }

    private JPAQuery<Long> createUserCountQuery(UserSearchCondition condition) {
        return queryFactory
                .select(siteUser.count())
                .from(siteUser)
                .where(
                        roleEq(condition.role()),
                        keywordContains(condition.keyword())
                );
    }

    @Override
    public Page<RestrictedUserSearchResponse> searchRestrictedUsers(
            RestrictedUserSearchCondition condition,
            Pageable pageable
    ) {
        List<RestrictedUserSearchResponse> content = queryFactory
                .select(RESTRICTED_USER_SEARCH_RESPONSE_PROJECTION)
                .from(siteUser)

                // 최신 신고 내역 조회
                .leftJoin(report).on(
                        report.reportedId.eq(siteUser.id)
                                .and(
                                        report.id.eq(
                                                JPAExpressions
                                                        .select(report.id.max())
                                                        .from(report)
                                                        .where(report.reportedId.eq(siteUser.id))
                                        )
                                )
                )

                // 최신 차단 내역 조회
                .leftJoin(userBan).on(
                        userBan.bannedUserId.eq(siteUser.id)
                                .and(
                                        userBan.id.eq(
                                                JPAExpressions
                                                        .select(userBan.id.max())
                                                        .from(userBan)
                                                        .where(userBan.bannedUserId.eq(siteUser.id))
                                        )
                                )
                )

                .where(
                        roleEq(condition.role()),
                        isRestrictedUser(),
                        userStatusEq(condition.userStatus()),
                        keywordContains(condition.keyword())
                )
                .orderBy(siteUser.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = createRestrictedUserCountQuery(condition).fetchOne();

        return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
    }


    private JPAQuery<Long> createRestrictedUserCountQuery(RestrictedUserSearchCondition condition) {
        return queryFactory
                .select(siteUser.count())
                .from(siteUser)
                .where(
                        roleEq(condition.role()),
                        isRestrictedUser(),
                        userStatusEq(condition.userStatus()),
                        keywordContains(condition.keyword())
                );
    }

    private BooleanExpression isRestrictedUser() {
        return siteUser.userStatus.in(
            UserStatus.REPORTED,
            UserStatus.BANNED
        );
    }

    private BooleanExpression roleEq(Role role) {
        return role != null ? siteUser.role.eq(role) : null;
    }

    private BooleanExpression userStatusEq(UserStatus userStatus) {
        return userStatus != null ? siteUser.userStatus.eq(userStatus) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!hasText(keyword)) {
            return null;
        }
        return siteUser.nickname.containsIgnoreCase(keyword);
    }

    @Override
    public UserInfoDetailResponse getUserInfoDetailByUserId(long userId) {
        SiteUser user = queryFactory
                .selectFrom(siteUser)
                .where(siteUser.id.eq(userId))
                .fetchOne();

        // 신고 내역
        List<ReportedHistoryResponse> reportedHistoryResponses = new ArrayList<>();
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            reportedHistoryResponses = fetchReportedHistories(userId);
        }

        if (user.getRole() == Role.MENTOR) {
            // 멘토 상세 내역
            MentorInfoResponse mentorInfoResponse = fetchMentorInfo(userId);
            return new UserInfoDetailResponse(mentorInfoResponse, null, reportedHistoryResponses);
        } else {
            // 멘티 상세 내역
            MenteeInfoResponse menteeInfoResponse = fetchMenteeInfo(userId);
            return new UserInfoDetailResponse(null, menteeInfoResponse, reportedHistoryResponses);
        }
    }

    @Override
    public RestrictedUserInfoDetailResponse getRestrictedUserInfoDetail(long userId) {
        List<ReportedHistoryResponse> reportedHistoryResponses = fetchReportedHistories(userId);
        List<BannedHistoryResponse> bannedHistoryResponses = fetchBannedHistories(userId);

        return new RestrictedUserInfoDetailResponse(reportedHistoryResponses, bannedHistoryResponses);
    }

    private List<ReportedHistoryResponse> fetchReportedHistories(long userId) {
        return queryFactory
                .select(REPORTED_HISTORY_RESPONSE_PROJECTION)
                .from(report)
                .where(report.reportedId.eq(userId))
                .orderBy(report.createdAt.desc())
                .fetch();
    }

    private List<BannedHistoryResponse> fetchBannedHistories(long userId) {
        return queryFactory
                .select(BANNED_HISTORY_RESPONSE_PROJECTION)
                .from(userBan)
                .where(userBan.bannedUserId.eq(userId))
                .orderBy(userBan.createdAt.desc())
                .fetch();
    }

    private MentorInfoResponse fetchMentorInfo(long userId) {
        Long mentorId = queryFactory
                .select(mentor.id)
                .from(mentor)
                .where(mentor.siteUserId.eq(userId))
                .fetchOne();

        List<MatchedInfoResponse> menteeInfos = new ArrayList<>();
        if (mentorId != null) {
            menteeInfos = queryFactory
                    .select(MATCHED_INFO_RESPONSE_PROJECTION)
                    .from(mentoring)
                    .join(siteUser).on(siteUser.id.eq(mentoring.menteeId))
                    .where(mentoring.mentorId.eq(mentorId))
                    .orderBy(mentoring.confirmedAt.desc())
                    .fetch();
        }

        List<MentorApplicationHistoryResponse> mentorApplicationHistory = queryFactory
                .select(MENTOR_APPLICATION_HISTORY_RESPONSE_PROJECTION)
                .from(mentorApplication)
                .where(mentorApplication.siteUserId.eq(userId))
                .orderBy(mentorApplication.createdAt.desc())
                .fetch();

        return new MentorInfoResponse(menteeInfos, mentorApplicationHistory);
    }

    private MenteeInfoResponse fetchMenteeInfo(long userId) {
        UnivApplyInfoResponse univApplyInfoResponse = fetchUnivApplyInfo(userId);
        List<MatchedInfoResponse> mentorInfos = queryFactory
                .select(MATCHED_INFO_RESPONSE_PROJECTION)
                .from(mentoring)
                .join(mentor).on(mentor.id.eq(mentoring.mentorId))
                .join(siteUser).on(siteUser.id.eq(mentor.siteUserId))
                .where(mentoring.menteeId.eq(userId))
                .orderBy(mentoring.confirmedAt.desc())
                .fetch();

        return new MenteeInfoResponse(univApplyInfoResponse, mentorInfos);
    }

    private UnivApplyInfoResponse fetchUnivApplyInfo(long userId) {
        UnivApplyInfoResponse result = queryFactory
                .select(UNIV_APPLY_INFO_RESPONSE_PROJECTION)
                .from(application)
                .leftJoin(firstChoiceUnivApplyInfo).on(firstChoiceUnivApplyInfo.id.eq(application.firstChoiceUnivApplyInfoId))
                .leftJoin(secondChoiceUnivApplyInfo).on(secondChoiceUnivApplyInfo.id.eq(application.secondChoiceUnivApplyInfoId))
                .leftJoin(thirdChoiceUnivApplyInfo).on(thirdChoiceUnivApplyInfo.id.eq(application.thirdChoiceUnivApplyInfoId))
                .where(application.siteUserId.eq(userId))
                .orderBy(application.createdAt.desc())
                .fetchFirst();

        if (result == null) {
            return new UnivApplyInfoResponse(null, null, null);
        }

        return result;
    }


}
