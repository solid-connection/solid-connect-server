package com.example.solidconnection.report.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_report_reporter_id_target_type_target_id",
                columnNames = {"reporter_id", "target_type", "target_id"}
        )
})
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "reporter_id")
    private long reporterId;

    @Column(name = "report_type")
    @Enumerated(value = EnumType.STRING)
    private ReportType reportType;

    @Column(name = "target_type")
    @Enumerated(value = EnumType.STRING)
    private TargetType targetType;

    @Column(name = "target_id")
    private long targetId;

    public Report(long reporterId, ReportType reportType, TargetType targetType, long targetId) {
        this.reportType = reportType;
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
    }
}
