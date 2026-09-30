package com.hotelbooking.hotel_booking.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class LocalProfileStartupTest {

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Test
    void localProfileStartsWithJsonEnvironmentConfiguration()
            throws SQLException {
        assertTrue(environment.acceptsProfiles(Profiles.of("local")));
        assertEquals("loaded-from-local-json",
                environment.getProperty("JSON_LOADER_TEST_VALUE"));
        try (var connection = dataSource.getConnection()) {
            assertTrue(connection.getMetaData().getURL()
                    .startsWith("jdbc:h2:mem:local_profile_test"));
        }
    }
}
