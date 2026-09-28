-- Safe, non-destructive migration for existing Supabase/PostgreSQL data.
-- Run this script before deploying the application version that maps the
-- booking_reference and payment_reference columns as NOT NULL.

BEGIN;

-- PostgreSQL 13+ provides gen_random_uuid(). Supabase supports this function.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. Add nullable UUID columns so existing rows remain valid during backfill.
ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS booking_reference UUID;

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS payment_reference UUID;

-- 2. Backfill only rows that do not already have a public reference.
UPDATE bookings
SET booking_reference = gen_random_uuid()
WHERE booking_reference IS NULL;

UPDATE payments
SET payment_reference = gen_random_uuid()
WHERE payment_reference IS NULL;

-- 3. Enforce the application invariant after every existing row is populated.
ALTER TABLE bookings
    ALTER COLUMN booking_reference SET NOT NULL;

ALTER TABLE payments
    ALTER COLUMN payment_reference SET NOT NULL;

-- 4. Unique indexes enforce non-reuse and provide efficient public lookups.
CREATE UNIQUE INDEX IF NOT EXISTS uk_bookings_booking_reference
    ON bookings (booking_reference);

CREATE UNIQUE INDEX IF NOT EXISTS uk_payments_payment_reference
    ON payments (payment_reference);

COMMIT;
