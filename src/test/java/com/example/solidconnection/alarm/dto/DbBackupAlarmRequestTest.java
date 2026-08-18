package com.example.solidconnection.alarm.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.alarm.domain.DbBackupAlarmType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DB 백업 알림 요청 테스트")
class DbBackupAlarmRequestTest {

    private static final String INSTANCE_ID = "i-0c5d57927ef9ca426";
    private static final Instant OCCURRED_AT = Instant.parse("2026-08-17T03:00:00Z");

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    @Nested
    @DisplayName("백업 스크립트가 보내는 요청 역직렬화")
    class 요청을_역직렬화한다 {

        @Test
        void 백업_스크립트의_json_을_역직렬화한다() throws Exception {
            // given
            String json = """
                    {
                      "type": "BINLOG_GAP_DETECTED",
                      "instanceId": "i-0c5d57927ef9ca426",
                      "occurredAt": "2026-08-17T03:00:00Z",
                      "detail": "binlog.000012 와 binlog.000014 사이 누락"
                    }""";

            // when
            DbBackupAlarmRequest request = objectMapper.readValue(json, DbBackupAlarmRequest.class);

            // then
            assertAll(
                    () -> assertThat(request.type()).isEqualTo(DbBackupAlarmType.BINLOG_GAP_DETECTED),
                    () -> assertThat(request.instanceId()).isEqualTo(INSTANCE_ID),
                    () -> assertThat(request.occurredAt()).isEqualTo(OCCURRED_AT),
                    () -> assertThat(request.detail()).contains("binlog.000012")
            );
        }

        @Test
        void 상세_내용이_없는_json_도_역직렬화한다() throws Exception {
            // given
            String json = """
                    {
                      "type": "DUMP_FAILED",
                      "instanceId": "i-0c5d57927ef9ca426",
                      "occurredAt": "2026-08-17T03:00:00Z"
                    }""";

            // when
            DbBackupAlarmRequest request = objectMapper.readValue(json, DbBackupAlarmRequest.class);

            // then
            assertAll(
                    () -> assertThat(request.type()).isEqualTo(DbBackupAlarmType.DUMP_FAILED),
                    () -> assertThat(request.detail()).isNull()
            );
        }
    }

    @Nested
    @DisplayName("요청 유효성 검증")
    class 요청을_검증한다 {

        @Test
        void 필수_값이_모두_있으면_검증을_통과한다() {
            // given
            DbBackupAlarmRequest request = new DbBackupAlarmRequest(
                    DbBackupAlarmType.DUMP_FAILED, INSTANCE_ID, OCCURRED_AT, null);

            // when
            Set<ConstraintViolation<DbBackupAlarmRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        void 알림_유형이_없으면_검증에_실패한다() {
            // given
            DbBackupAlarmRequest request = new DbBackupAlarmRequest(null, INSTANCE_ID, OCCURRED_AT, null);

            // when
            Set<ConstraintViolation<DbBackupAlarmRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        void 인스턴스_식별자가_비어_있으면_검증에_실패한다() {
            // given
            DbBackupAlarmRequest request = new DbBackupAlarmRequest(
                    DbBackupAlarmType.DUMP_FAILED, "  ", OCCURRED_AT, null);

            // when
            Set<ConstraintViolation<DbBackupAlarmRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        void 발생_시각이_없으면_검증에_실패한다() {
            // given
            DbBackupAlarmRequest request = new DbBackupAlarmRequest(
                    DbBackupAlarmType.DUMP_FAILED, INSTANCE_ID, null, null);

            // when
            Set<ConstraintViolation<DbBackupAlarmRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        void 상세_내용이_1000자를_넘으면_검증에_실패한다() {
            // given
            DbBackupAlarmRequest request = new DbBackupAlarmRequest(
                    DbBackupAlarmType.DUMP_FAILED, INSTANCE_ID, OCCURRED_AT, "e".repeat(1001));

            // when
            Set<ConstraintViolation<DbBackupAlarmRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }
    }
}
