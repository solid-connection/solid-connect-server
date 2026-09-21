package com.example.solidconnection.siteuser.repository.custom;

import static com.example.solidconnection.application.domain.QApplication.application;
import static com.example.solidconnection.mentor.domain.QMentor.mentor;
import static com.example.solidconnection.mentor.domain.QMentorApplication.mentorApplication;
import static com.example.solidconnection.mentor.domain.QMentoring.mentoring;
import static com.example.solidconnection.report.domain.QReport.report;
import static com.example.solidconnection.siteuser.domain.QSiteUser.siteUser;
import static com.example.solidconnection.siteuser.domain.QUserBan.userBan;
import static com.example.solidconnection.university.domain.QUnivApplyInfo.univApplyInfo;
import static java.time.ZoneOffset.UTC;
import static org.springframework.util.StringUtils.hasText;

import com.example.solidconnection.admin.dto.BannedHistoryResponse;
import com.example.solidconnection.admin.dto.BannedInfoResponse;
import com.example.solidconnection.admin.dto.MatchedInfoResponse;
import com.example.solidconnection.admin.dto.MenteeInfoResponse;
import com.example.solidconnection.admin.dto.MentorApplicationHistoryInfoResponse;
import com.example.solidconnection.admin.dto.MentorInfoResponse;
import com.example.solidconnection.admin.dto.ReportedHistoryResponse;
import com.example.solidconnection.admin.dto.ReportedInfoResponse;
import com.example.solidconnection.admin.dto.RestrictedUserInfoDetailResponse;
import com.example.solidconnection.admin.dto.RestrictedUserSearchCondition;
import com.example.solidconnection.admin.dto.RestrictedUserSearchResponse;
import com.example.solidconnection.admin.dto.UnivApplyInfoResponse;
import com.example.solidconnection.admin.dto.UserInfoDetailResponse;
import com.example.solidconnection.admin.dto.UserSearchCondition;
import com.example.solidconnection.admin.dto.UserSearchResponse;
import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.ApplicationChoice;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.UserBanDuration;
import com.example.solidconnection.siteuser.domain.UserStatus;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class SiteUserFilterRepositoryImpl implements SiteUserFilterRepository {

    private static final ConstructorExpression<UserSearchResponse> USER_SEARCH_RESPONSE_PROJECTION =
            Projections.constructor(
                    UserSearchResponse.class,
                    siteUser.id,
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
                    siteUser.id,
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

    private static final ConstructorExpression<MentorApplicationHistoryInfoResponse> MENTOR_APPLICATION_HISTORY_RESPONSE_PROJECTION =
            Projections.constructor(
                    MentorApplicationHistoryInfoResponse.class,
                    mentorApplication.mentorApplicationStatus,
                    mentorApplication.rejectedReason,
                    mentorApplication.createdAt
            );

    private static final ConstructorExpression<BannedHistoryResponse> BANNED_HISTORY_RESPONSE_PROJECTION =
            Projections.constructor(
                    BannedHistoryResponse.class,
                    userBan.createdAt
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

    // 1차 쿼리 개선(2026-09-21, 미커밋 로컬 검증용):
    // 기존에는 siteUser 각 row마다 report 테이블 전체를 훑는 상관 서브쿼리(MAX(report.id) WHERE reported_id=...)를
    // leftJoin으로 실행해서, 페이지당 20건이라도 report(대량 테이블)를 20번 반복 스캔했다.
    // -> siteUser를 먼저 페이징해서 "이 페이지에 필요한 20개 id"를 확정한 뒤,
    //    report/userBan은 그 id 목록(IN절)에 대해서만 한 번씩 배치 조회하도록 분리했다.
    //    (MentorBatchQueryRepository 등 기존 코드베이스의 배치조회 패턴과 동일)
    @Override
    public Page<RestrictedUserSearchResponse> searchRestrictedUsers(
            RestrictedUserSearchCondition condition,
            Pageable pageable
    ) {
        List<SiteUser> siteUsers = queryFactory
                .selectFrom(siteUser)
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

        List<Long> siteUserIds = siteUsers.stream().map(SiteUser::getId).toList();

        Map<Long, ReportedInfoResponse> latestReportedInfoBySiteUserId = findLatestReportedInfoBySiteUserIds(siteUserIds);
        Map<Long, UserBanDuration> activeBanDurationBySiteUserId = findActiveBanDurationBySiteUserIds(siteUserIds);

        List<RestrictedUserSearchResponse> content = siteUsers.stream()
                .map(su -> new RestrictedUserSearchResponse(
                        su.getId(),
                        su.getNickname(),
                        su.getRole(),
                        su.getUserStatus(),
                        latestReportedInfoBySiteUserId.get(su.getId()),
                        new BannedInfoResponse(
                                su.getUserStatus() == UserStatus.BANNED,
                                activeBanDurationBySiteUserId.get(su.getId())
                        )
                ))
                .toList();

        Long totalCount = createRestrictedUserCountQuery(condition).fetchOne();

        return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
    }

    private Map<Long, ReportedInfoResponse> findLatestReportedInfoBySiteUserIds(List<Long> siteUserIds) {
        if (siteUserIds.isEmpty()) {
            return Map.of();
        }
        return queryFactory
                .select(report.reportedId, REPORTED_INFO_RESPONSE_PROJECTION)
                .from(report)
                .where(
                        report.reportedId.in(siteUserIds),
                        report.id.in(
                                JPAExpressions
                                        .select(report.id.max())
                                        .from(report)
                                        .where(report.reportedId.in(siteUserIds))
                                        .groupBy(report.reportedId)
                        )
                )
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(report.reportedId),
                        tuple -> tuple.get(REPORTED_INFO_RESPONSE_PROJECTION)
                ));
    }

    private Map<Long, UserBanDuration> findActiveBanDurationBySiteUserIds(List<Long> siteUserIds) {
        if (siteUserIds.isEmpty()) {
            return Map.of();
        }
        // PR 리뷰 반영(2026-09-21): 동시 요청으로 같은 유저에게 활성 차단이 2건 이상 생길 수 있어(user_ban에
        // 유저당 활성 차단 1건 제약이 없고, validateNotAlreadyBanned도 check-then-act라 race가 가능)
        // 단순 toMap은 중복 키에서 IllegalStateException을 던진다. expiredAt 내림차순으로 정렬해
        // 가장 나중에 만료되는 차단을 남기는 merge function을 추가했다.
        return queryFactory
                .select(userBan.bannedUserId, userBan.duration)
                .from(userBan)
                .where(
                        userBan.bannedUserId.in(siteUserIds),
                        userBan.isExpired.eq(false),
                        userBan.expiredAt.after(ZonedDateTime.now(UTC))
                )
                .orderBy(userBan.expiredAt.desc())
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(userBan.bannedUserId),
                        tuple -> tuple.get(userBan.duration),
                        (first, duplicate) -> first
                ));
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
    public UserInfoDetailResponse getUserInfoDetailByUserId(SiteUser user) {
        // 신고 내역
        List<ReportedHistoryResponse> reportedHistoryResponses = new ArrayList<>();
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            reportedHistoryResponses = fetchReportedHistories(user.getId());
        }

        if (user.getRole() == Role.MENTOR) {
            // 멘토 상세 내역
            MentorInfoResponse mentorInfoResponse = fetchMentorInfo(user.getId());
            return new UserInfoDetailResponse(mentorInfoResponse, null, reportedHistoryResponses);
        } else {
            // 멘티 상세 내역
            MenteeInfoResponse menteeInfoResponse = fetchMenteeInfo(user.getId());
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

        List<MentorApplicationHistoryInfoResponse> mentorApplicationHistory = queryFactory
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
        Application latestApplication = queryFactory
                .selectFrom(application)
                .where(application.siteUserId.eq(userId), application.isDelete.isFalse())
                .orderBy(application.createdAt.desc())
                .fetchFirst();

        if (latestApplication == null) {
            return new UnivApplyInfoResponse(List.of());
        }

        List<Long> univApplyInfoIds = latestApplication.getChoices().stream()
                .sorted(Comparator.comparingInt(ApplicationChoice::getChoiceOrder))
                .map(ApplicationChoice::getUnivApplyInfoId)
                .toList();

        if (univApplyInfoIds.isEmpty()) {
            return new UnivApplyInfoResponse(List.of());
        }

        List<Tuple> tuples = queryFactory
                .select(univApplyInfo.id, univApplyInfo.koreanName)
                .from(univApplyInfo)
                .where(univApplyInfo.id.in(univApplyInfoIds))
                .fetch();

        Map<Long, String> nameById = tuples.stream()
                .collect(Collectors.toMap(
                        t -> t.get(univApplyInfo.id),
                        t -> t.get(univApplyInfo.koreanName)
                ));

        List<String> choiceNames = univApplyInfoIds.stream()
                .map(nameById::get)
                .filter(Objects::nonNull)
                .toList();

        return new UnivApplyInfoResponse(choiceNames);
    }


}
