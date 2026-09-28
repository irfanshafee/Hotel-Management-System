-- Safe migration for existing Supabase room records.
-- Run once before deploying the optimistic-locking booking change.
ALTER TABLE rooms
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
