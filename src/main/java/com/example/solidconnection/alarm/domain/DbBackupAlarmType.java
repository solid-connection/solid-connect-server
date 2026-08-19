package com.example.solidconnection.alarm.domain;

import lombok.Getter;

/*
 * - 심각도는 호출자가 임의로 낮출 수 없도록 요청 값이 아니라 유형에서 결정한다.
 * - 지연은 아직 복구 여지가 있어 경고로 두고, 기준점이나 복구 체인이 깨지는 경우는 심각으로 둔다.
 * */
@Getter
public enum DbBackupAlarmType {

    DUMP_FAILED("전체 덤프 실패", DbBackupAlarmSeverity.CRITICAL),
    BINLOG_UPLOAD_FAILED("바이너리 로그 업로드 실패", DbBackupAlarmSeverity.CRITICAL),
    BINLOG_GAP_DETECTED("바이너리 로그 누락", DbBackupAlarmSeverity.CRITICAL),
    BINLOG_UPLOAD_DELAYED("바이너리 로그 업로드 지연", DbBackupAlarmSeverity.WARNING),
    ;

    private final String displayName;
    private final DbBackupAlarmSeverity severity;

    DbBackupAlarmType(String displayName, DbBackupAlarmSeverity severity) {
        this.displayName = displayName;
        this.severity = severity;
    }
}
