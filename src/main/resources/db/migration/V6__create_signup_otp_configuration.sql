CREATE TABLE IF NOT EXISTS "Production".signup_otp_configuration (
    id SMALLINT PRIMARY KEY,
    validity_minutes INTEGER NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_signup_otp_configuration_singleton CHECK (id = 1),
    CONSTRAINT ck_signup_otp_validity_minutes CHECK (
        validity_minutes BETWEEN 1 AND 1440
    )
);

INSERT INTO "Production".signup_otp_configuration (
    id, validity_minutes
) VALUES (1, 5)
ON CONFLICT (id) DO NOTHING;
