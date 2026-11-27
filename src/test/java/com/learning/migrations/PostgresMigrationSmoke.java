package com.learning.migrations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = PostgresMigrationSmoke.Config.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
public class PostgresMigrationSmoke {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Configuration
    @EnableAutoConfiguration
    static class Config {
    }

    @Test
    void connectsToExpectedDatabase() {
        String databaseName = System.getenv("BOOKING_EXPECTED_DATABASE");
        String actualDatabase = jdbcTemplate.queryForObject(
                "SELECT current_database()", String.class);

        assertEquals(databaseName, actualDatabase);
    }

}
