package com.example.solidconnection.report.repository;

import com.example.solidconnection.report.domain.Report;
import com.example.solidconnection.report.domain.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetTypeAndTargetId(long reporterId, TargetType targetType, long targetId);

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Report r
            WHERE (r.targetType = 'POST' AND EXISTS (
                SELECT 1 FROM Post p WHERE p.id = r.targetId AND p.siteUserId = :userId
            ))
            OR (r.targetType = 'CHAT' AND EXISTS (
                SELECT 1 FROM ChatMessage cm
                JOIN ChatParticipant cp ON cp.id = cm.senderId
                WHERE cm.id = r.targetId AND cp.siteUserId = :userId
            ))
            """)
    boolean existsReportByUserId(@Param("userId") long userId);
}
