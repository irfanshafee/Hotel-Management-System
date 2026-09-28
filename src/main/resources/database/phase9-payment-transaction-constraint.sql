-- Run once in the Supabase SQL editor after checking for existing duplicate PAID IDs.
-- A partial index allows FAILED attempts to retain repeated invalid submitted values.
CREATE UNIQUE INDEX IF NOT EXISTS uk_payments_paid_transaction_id
    ON payments (transaction_id)
    WHERE status = 'PAID' AND transaction_id IS NOT NULL;
