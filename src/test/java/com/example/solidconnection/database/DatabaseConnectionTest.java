package com.example.solidconnection.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DataJpaTest
class DatabaseConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private DatabaseMetaData metaData;

    @DisplayName("데이터베이스 연결 및 테이블 존재 여부 테스트")
    @Test
    void connectDatabaseAndCheckTables() {
        assertThatCode(() -> metaData = Objects.requireNonNull(jdbcTemplate.getDataSource())
                .getConnection()
                .getMetaData())
                .doesNotThrowAnyException();

        assertAll(
                () -> assertThat(isTableExist("SITE_USER")).isTrue(),
                () -> assertThat(isTableExist("COUNTRY")).isTrue(),
                () -> assertThat(isTableExist("INTERESTED_COUNTRY")).isTrue(),
                () -> assertThat(isTableExist("REGION")).isTrue(),
                () -> assertThat(isTableExist("INTERESTED_REGION")).isTrue(),
                () -> assertThat(isTableExist("LANGUAGE_REQUIREMENT")).isTrue(),
                () -> assertThat(isTableExist("UNIVERSITY")).isTrue(),
                () -> assertThat(isTableExist("LIKED_UNIVERSITY")).isTrue(),
                () -> assertThat(isTableExist("UNIVERSITY_INFO_FOR_APPLY")).isTrue()
        );
    }

    private boolean isTableExist(String tableName) throws SQLException {
        return metaData.getTables(null, null, tableName, null).next();
    }
}
