package com.example.solidconnection.e2e;

import com.example.solidconnection.support.DatabaseClearExtension;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.web.server.LocalServerPort;

@TestContainerSpringBootTest
@ExtendWith(DatabaseClearExtension.class)
abstract class BaseEndToEndTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }
}
