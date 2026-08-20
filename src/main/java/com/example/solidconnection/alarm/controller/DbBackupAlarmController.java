package com.example.solidconnection.alarm.controller;

import com.example.solidconnection.alarm.dto.DbBackupAlarmRequest;
import com.example.solidconnection.alarm.service.DbBackupAlarmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/alarms")
@RequiredArgsConstructor
public class DbBackupAlarmController {

    private static final String INTERNAL_ALARM_TOKEN_HEADER = "X-Internal-Alarm-Token";

    private final DbBackupAlarmService dbBackupAlarmService;

    // DB EC2 의 백업 실패 이벤트를 받아 Discord 로 알리는 내부 전용 api
    @PostMapping("/db-backup")
    public ResponseEntity<Void> alarmBackupFailure(
            @RequestHeader(value = INTERNAL_ALARM_TOKEN_HEADER, required = false) String token,
            @Valid @RequestBody DbBackupAlarmRequest dbBackupAlarmRequest
    ) {
        dbBackupAlarmService.alarmBackupFailure(token, dbBackupAlarmRequest);
        return ResponseEntity.accepted().build();
    }
}
