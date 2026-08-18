package com.example.solidconnection.alarm.dto;

import com.example.solidconnection.alarm.domain.DbBackupAlarmType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record DbBackupAlarmRequest(

        @NotNull
        DbBackupAlarmType type,

        @NotBlank
        @Size(max = 64)
        String instanceId,

        @NotNull
        Instant occurredAt,

        @Size(max = 1000)
        String detail
) {

}
