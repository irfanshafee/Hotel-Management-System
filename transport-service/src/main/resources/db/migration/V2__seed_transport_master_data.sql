INSERT INTO driver (name, phone_number, nid_number, status) VALUES
    ('Abdul Karim', '01700000001', 'DUMMY-NID-0001', 'ACTIVE'),
    ('Mohammad Rahim', '01700000002', 'DUMMY-NID-0002', 'ACTIVE'),
    ('Shahidul Islam', '01700000003', 'DUMMY-NID-0003', 'ACTIVE'),
    ('Kamal Hossain', '01700000004', 'DUMMY-NID-0004', 'ACTIVE'),
    ('Jamal Uddin', '01700000005', 'DUMMY-NID-0005', 'ACTIVE'),
    ('Rafiqul Hasan', '01700000006', 'DUMMY-NID-0006', 'ACTIVE');

INSERT INTO vehicle (bus_number, name, total_seats, status) VALUES
    ('DHAKA-METRO-B-1001', 'Travel Express 1', 20, 'ACTIVE'),
    ('DHAKA-METRO-B-1002', 'Travel Express 2', 20, 'ACTIVE'),
    ('DHAKA-METRO-B-1003', 'Travel Express 3', 20, 'ACTIVE'),
    ('DHAKA-METRO-B-1004', 'Travel Express 4', 15, 'ACTIVE'),
    ('DHAKA-METRO-B-1005', 'Travel Express 5', 15, 'ACTIVE'),
    ('DHAKA-METRO-B-1006', 'Travel Express 6', 15, 'ACTIVE'),
    ('DHAKA-METRO-B-1007', 'Travel Express 7', 20, 'ACTIVE'),
    ('DHAKA-METRO-B-1008', 'Travel Express 8', 20, 'ACTIVE'),
    ('DHAKA-METRO-B-1009', 'Travel Express 9', 20, 'ACTIVE'),
    ('DHAKA-METRO-B-1010', 'Travel Express 10', 15, 'ACTIVE'),
    ('DHAKA-METRO-B-1011', 'Travel Express 11', 15, 'ACTIVE'),
    ('DHAKA-METRO-B-1012', 'Travel Express 12', 15, 'ACTIVE');

INSERT INTO route (origin, destination, status) VALUES
    ('Dhaka', 'Chattogram', 'ACTIVE'),
    ('Chattogram', 'Dhaka', 'ACTIVE'),
    ('Dhaka', 'Sylhet', 'ACTIVE'),
    ('Sylhet', 'Dhaka', 'ACTIVE'),
    ('Dhaka', 'Rajshahi', 'ACTIVE'),
    ('Rajshahi', 'Dhaka', 'ACTIVE');

-- Seat labels are deterministic: A1, B1, C1, D1, A2, and so on.
-- Each vehicle receives exactly vehicle.total_seats rows.
INSERT INTO seat (vehicle_id, seat_number)
SELECT vehicle.id,
       chr(65 + ((seat_position - 1) % 4)::INTEGER)
           || (((seat_position - 1) / 4) + 1)::TEXT
FROM vehicle
CROSS JOIN LATERAL generate_series(1, vehicle.total_seats) AS seat_position;
