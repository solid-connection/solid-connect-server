package com.example.solidconnection.database;

import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@TestContainerSpringBootTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.baseline-on-migrate=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@DirtiesContext(classMode = BEFORE_CLASS)
class FlywayMigrationTest {

    @Test
    void flyway_스크립트가_정상적으로_수행되는지_확인한다() {

    }
}
