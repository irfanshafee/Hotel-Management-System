package com.hotelbooking.hotel_booking.database;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionDatabaseConfigurationTest {

    @Test
    void productionDisablesHibernateDdlAndUsesQuotedSchema() throws IOException {
        PropertySource<?> prod = loadYaml("application-prod.yml");

        assertEquals("none", prod.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals(false, prod.getProperty("spring.jpa.show-sql"));
        assertEquals(true, prod.getProperty("spring.flyway.enabled"));
        assertEquals("Production",
                prod.getProperty("spring.flyway.default-schema"));
        assertEquals("\"Production\"",
                prod.getProperty(
                        "spring.jpa.properties.hibernate.default_schema"));
    }

    @Test
    void localKeepsUpdateAndDoesNotRunProductionMigrations()
            throws IOException {
        PropertySource<?> local = loadYaml("application-local.yml");

        assertEquals("update",
                local.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals(false, local.getProperty("spring.flyway.enabled"));
    }

    @Test
    void initialMigrationIsNonDestructiveAndSchemaQualified()
            throws IOException {
        String sql = resourceText(
                "db/migration/V1__initial_production_schema.sql");
        String executableSql = sql.replaceAll("(?m)--.*$", " ")
                .toLowerCase();

        assertFalse(executableSql.matches("(?s).*\\bdrop\\b.*"));
        assertFalse(executableSql.matches("(?s).*\\btruncate\\b.*"));
        assertFalse(executableSql.matches("(?s).*\\bdelete\\s+from\\b.*"));
        assertTrue(sql.contains("CREATE SCHEMA IF NOT EXISTS \"Production\""));
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS \"Production\".users"));
        assertTrue(sql.contains("REFERENCES \"Production\".bookings (id)"));
        assertFalse(sql.contains("public."));
    }

    private PropertySource<?> loadYaml(String resource) throws IOException {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader()
                .load(resource, new ClassPathResource(resource));
        return sources.getFirst();
    }

    private String resourceText(String path) throws IOException {
        try (var input = new ClassPathResource(path).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
