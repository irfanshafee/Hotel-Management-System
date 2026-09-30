-- One-time, non-destructive migration for the existing Supabase database.
-- The application switches to the quoted "Production" schema, while the
-- current data remains untouched in public.
DO $$
BEGIN
    IF to_regclass('public.users') IS NULL
       OR to_regclass('public.hotels') IS NULL
       OR to_regclass('public.rooms') IS NULL
       OR to_regclass('public.bookings') IS NULL
       OR to_regclass('public.payments') IS NULL THEN
        RAISE NOTICE 'No complete public hotel-booking schema found; skipping data copy.';
        RETURN;
    END IF;

    INSERT INTO "Production".users (
        id, name, age, email, password, role, created_at
    )
    SELECT id, name, age, email, password, role, created_at
    FROM public.users
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO "Production".hotels (
        id, name, city, address, description
    )
    SELECT id, name, city, address, description
    FROM public.hotels
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO "Production".rooms (
        id, version, room_number, hotel_id, category, capacity, price
    )
    SELECT id, 0, room_number, hotel_id, category, capacity, price
    FROM public.rooms
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO "Production".bookings (
        id, booking_uuid, user_id, room_id, start_date, end_date, status, created_at
    )
    SELECT id, gen_random_uuid(), user_id, room_id, start_date, end_date, status, created_at
    FROM public.bookings
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO "Production".payments (
        id, payment_uuid, booking_id, amount, paid_amount, remaining_amount,
        method, status, transaction_id, created_at
    )
    SELECT id, gen_random_uuid(), booking_id, amount, paid_amount, remaining_amount,
           method, status, transaction_id, created_at
    FROM public.payments
    ON CONFLICT (id) DO NOTHING;

    PERFORM setval('"Production".users_id_seq',
                   GREATEST(COALESCE((SELECT MAX(id) FROM "Production".users), 1), 1),
                   true);
    PERFORM setval('"Production".hotels_id_seq',
                   GREATEST(COALESCE((SELECT MAX(id) FROM "Production".hotels), 1), 1),
                   true);
    PERFORM setval('"Production".rooms_id_seq',
                   GREATEST(COALESCE((SELECT MAX(id) FROM "Production".rooms), 1), 1),
                   true);
    PERFORM setval('"Production".bookings_id_seq',
                   GREATEST(COALESCE((SELECT MAX(id) FROM "Production".bookings), 1), 1),
                   true);
    PERFORM setval('"Production".payments_id_seq',
                   GREATEST(COALESCE((SELECT MAX(id) FROM "Production".payments), 1), 1),
                   true);
END $$;
