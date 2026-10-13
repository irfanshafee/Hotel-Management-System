package com.hotelbooking.transport.database;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationContractTest {

    @Test
    void schemaDefinesSevenCoreTablesAndConcurrencyConstraint() throws IOException {
        String migration = readMigration("V1__create_transport_schema.sql");

        assertEquals(7, occurrences(migration, "CREATE TABLE "));
        assertTrue(migration.contains("CREATE TABLE driver"));
        assertTrue(migration.contains("CREATE TABLE vehicle"));
        assertTrue(migration.contains("CREATE TABLE route"));
        assertTrue(migration.contains("CREATE TABLE seat"));
        assertTrue(migration.contains("CREATE TABLE trip"));
        assertTrue(migration.contains("CREATE TABLE transport_booking"));
        assertTrue(migration.contains("CREATE TABLE booking_seat"));
        assertTrue(migration.contains("ON booking_seat (trip_id, seat_id)"));
        assertTrue(migration.contains("WHERE allocation_status = 'ACTIVE'"));
        assertTrue(migration.contains("FOREIGN KEY (booking_id, trip_id)"));
    }

    @Test
    void userIdIsAnExternalReferenceWithoutDatabaseForeignKey() throws IOException {
        String migration = readMigration("V1__create_transport_schema.sql");

        assertTrue(migration.contains("user_id BIGINT NOT NULL"));
        assertFalse(migration.contains("FOREIGN KEY (user_id)"));
    }

    private String readMigration(String name) throws IOException {
        try (var stream = getClass().getClassLoader()
                .getResourceAsStream("db/migration/" + name)) {
            if (stream == null) {
                throw new IOException("Migration not found: " + name);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private int occurrences(String text, String value) {
        return (text.length() - text.replace(value, "").length()) / value.length();
    }
}
