package com.example.solidconnection.alarm.domain;

import lombok.Getter;

@Getter
public enum DbBackupAlarmSeverity {

    WARNING("경고"),
    CRITICAL("심각"),
    ;

    private final String displayName;

    DbBackupAlarmSeverity(String displayName) {
        this.displayName = displayName;
    }
}
