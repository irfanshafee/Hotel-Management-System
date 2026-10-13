-- Complete the city network used by the Transport UI.
INSERT INTO route (origin, destination, status)
SELECT route_data.origin, route_data.destination, 'ACTIVE'
FROM (VALUES
    ('Chattogram', 'Sylhet'), ('Sylhet', 'Chattogram'),
    ('Chattogram', 'Rajshahi'), ('Rajshahi', 'Chattogram'),
    ('Sylhet', 'Rajshahi'), ('Rajshahi', 'Sylhet')
) AS route_data(origin, destination)
WHERE NOT EXISTS (
    SELECT 1 FROM route existing
    WHERE lower(existing.origin) = lower(route_data.origin)
      AND lower(existing.destination) = lower(route_data.destination)
);

-- Seed every ordered city pair for the next seven days. Each route has a
-- dedicated vehicle and driver so the demo schedule contains no conflicts.
INSERT INTO trip (
    vehicle_id, route_id, driver_id, departure_time, arrival_time, fare, status
)
SELECT vehicle.id,
       route.id,
       driver.id,
       ((CURRENT_DATE + schedule_days.day_offset) + schedule.departure)::TIMESTAMP
           AT TIME ZONE 'Asia/Dhaka',
       ((CURRENT_DATE + schedule_days.day_offset) + schedule.departure
           + schedule.duration)::TIMESTAMP AT TIME ZONE 'Asia/Dhaka',
       schedule.fare,
       'SCHEDULED'
FROM (VALUES
    ('DHAKA-METRO-B-1001', 'DUMMY-NID-0001', 'Dhaka',      'Chattogram', TIME '06:00', INTERVAL '6 hours',   1200.00),
    ('DHAKA-METRO-B-1002', 'DUMMY-NID-0002', 'Chattogram', 'Dhaka',      TIME '07:00', INTERVAL '6 hours',   1200.00),
    ('DHAKA-METRO-B-1003', 'DUMMY-NID-0003', 'Dhaka',      'Sylhet',     TIME '07:30', INTERVAL '5 hours',    900.00),
    ('DHAKA-METRO-B-1004', 'DUMMY-NID-0004', 'Sylhet',     'Dhaka',      TIME '08:00', INTERVAL '5 hours',    900.00),
    ('DHAKA-METRO-B-1005', 'DUMMY-NID-0005', 'Dhaka',      'Rajshahi',   TIME '08:30', INTERVAL '5.5 hours', 1000.00),
    ('DHAKA-METRO-B-1006', 'DUMMY-NID-0006', 'Rajshahi',   'Dhaka',      TIME '09:00', INTERVAL '5.5 hours', 1000.00),
    ('DHAKA-METRO-B-1007', 'DUMMY-NID-0001', 'Chattogram', 'Sylhet',     TIME '06:30', INTERVAL '7 hours',   1350.00),
    ('DHAKA-METRO-B-1008', 'DUMMY-NID-0002', 'Sylhet',     'Chattogram', TIME '07:15', INTERVAL '7 hours',   1350.00),
    ('DHAKA-METRO-B-1009', 'DUMMY-NID-0003', 'Chattogram', 'Rajshahi',   TIME '06:45', INTERVAL '8 hours',   1500.00),
    ('DHAKA-METRO-B-1010', 'DUMMY-NID-0004', 'Rajshahi',   'Chattogram', TIME '07:45', INTERVAL '8 hours',   1500.00),
    ('DHAKA-METRO-B-1011', 'DUMMY-NID-0005', 'Sylhet',     'Rajshahi',   TIME '08:15', INTERVAL '7 hours',   1300.00),
    ('DHAKA-METRO-B-1012', 'DUMMY-NID-0006', 'Rajshahi',   'Sylhet',     TIME '09:15', INTERVAL '7 hours',   1300.00)
) AS schedule (
    bus_number, nid_number, origin, destination, departure, duration, fare
)
CROSS JOIN generate_series(1, 7) AS schedule_days(day_offset)
JOIN vehicle ON vehicle.bus_number = schedule.bus_number
JOIN driver ON driver.nid_number = schedule.nid_number
JOIN route ON route.origin = schedule.origin
          AND route.destination = schedule.destination
WHERE NOT EXISTS (
    SELECT 1 FROM trip existing
    WHERE existing.vehicle_id = vehicle.id
      AND existing.departure_time =
          ((CURRENT_DATE + schedule_days.day_offset) + schedule.departure)::TIMESTAMP
              AT TIME ZONE 'Asia/Dhaka'
);
