package com.example.solidconnection.support.integration;

import com.example.solidconnection.support.DatabaseClearExtension;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@ExtendWith(DatabaseClearExtension.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected TestDataSetUpHelper testDataSetUpHelper;

    @BeforeEach
    public void setUpBaseData() {
        testDataSetUpHelper.setUpBasicData();
    }
}
