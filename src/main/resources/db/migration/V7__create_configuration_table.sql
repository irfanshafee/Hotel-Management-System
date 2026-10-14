-- Generic key/value application settings. Replaces the single-purpose
-- signup_otp_configuration table: each setting is one row, read by its key.
CREATE TABLE IF NOT EXISTS "Production".configuration_table (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value VARCHAR(500) NOT NULL,
    description VARCHAR(500),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Carry over the current OTP validity (it may have been changed by an admin),
-- but only if the old table still exists: it may already have been migrated
-- and dropped by hand.
DO $$
BEGIN
    IF to_regclass('"Production".signup_otp_configuration') IS NOT NULL THEN
        INSERT INTO "Production".configuration_table (config_key, config_value, description, updated_at)
        SELECT 'signup.otp.validity.minutes',
               validity_minutes::text,
               'How long a signup verification code stays valid, in minutes (1-1440).',
               updated_at
        FROM "Production".signup_otp_configuration
        WHERE id = 1
        ON CONFLICT (config_key) DO NOTHING;
    END IF;
END $$;

-- Default when the old row did not exist.
INSERT INTO "Production".configuration_table (config_key, config_value, description)
VALUES ('signup.otp.validity.minutes', '5',
        'How long a signup verification code stays valid, in minutes (1-1440).')
ON CONFLICT (config_key) DO NOTHING;

DROP TABLE IF EXISTS "Production".signup_otp_configuration;
