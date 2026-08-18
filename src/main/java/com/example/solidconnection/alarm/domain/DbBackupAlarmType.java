package com.example.solidconnection.alarm.domain;

import lombok.Getter;

@Getter
public enum DbBackupAlarmType {

    DUMP_FAILED("전체 덤프 실패"),
    BINLOG_UPLOAD_FAILED("바이너리 로그 업로드 실패"),
    BINLOG_GAP_DETECTED("바이너리 로그 누락"),
    BINLOG_UPLOAD_DELAYED("바이너리 로그 업로드 지연"),
    ;

    private final String displayName;

    DbBackupAlarmType(String displayName) {
        this.displayName = displayName;
    }
}
