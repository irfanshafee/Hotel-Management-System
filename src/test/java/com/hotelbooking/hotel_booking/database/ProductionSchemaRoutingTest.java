package com.hotelbooking.hotel_booking.database;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("prod")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "DB_URL=jdbc:h2:mem:production_schema_routing;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
                "DB_USERNAME=sa",
                "DB_PASSWORD=",
                "JWT_SECRET=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.hikari.connection-init-sql=CREATE SCHEMA IF NOT EXISTS \"Production\"",
                "spring.flyway.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create",
                "spring.jpa.show-sql=false"
        })
class ProductionSchemaRoutingTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void hibernateRoutesEntityTablesToExactProductionSchema() {
        Integer tableCount = jdbcTemplate.queryForObject("""
                select count(*)
                  from information_schema.tables
                 where table_schema = 'Production'
                   and table_name in
                       ('users', 'hotels', 'rooms', 'bookings', 'payments')
                """, Integer.class);

        assertEquals(5, tableCount);
    }
}
