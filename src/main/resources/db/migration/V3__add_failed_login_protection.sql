-- Preserve all existing users while adding temporary failed-login protection.
ALTER TABLE "Production".users
    ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER;

ALTER TABLE "Production".users
    ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP WITHOUT TIME ZONE;

UPDATE "Production".users
SET failed_login_attempts = 0
WHERE failed_login_attempts IS NULL;

ALTER TABLE "Production".users
    ALTER COLUMN failed_login_attempts SET DEFAULT 0;

ALTER TABLE "Production".users
    ALTER COLUMN failed_login_attempts SET NOT NULL;
