INSERT INTO trip (
    vehicle_id, route_id, driver_id, departure_time, arrival_time, fare, status
)
SELECT vehicle.id, route.id, driver.id,
       trip_data.departure_time, trip_data.arrival_time,
       trip_data.fare, 'SCHEDULED'
FROM (VALUES
    ('DHAKA-METRO-B-1001', 'Dhaka', 'Chattogram', 'DUMMY-NID-0001',
     TIMESTAMPTZ '2027-01-01 06:00:00+06', TIMESTAMPTZ '2027-01-01 12:00:00+06', 1200.00),
    ('DHAKA-METRO-B-1001', 'Chattogram', 'Dhaka', 'DUMMY-NID-0002',
     TIMESTAMPTZ '2027-01-01 18:00:00+06', TIMESTAMPTZ '2027-01-02 00:00:00+06', 1200.00),
    ('DHAKA-METRO-B-1001', 'Dhaka', 'Chattogram', 'DUMMY-NID-0001',
     TIMESTAMPTZ '2027-01-03 06:00:00+06', TIMESTAMPTZ '2027-01-03 12:00:00+06', 1200.00),
    ('DHAKA-METRO-B-1001', 'Chattogram', 'Dhaka', 'DUMMY-NID-0002',
     TIMESTAMPTZ '2027-01-03 18:00:00+06', TIMESTAMPTZ '2027-01-04 00:00:00+06', 1200.00),

    ('DHAKA-METRO-B-1002', 'Dhaka', 'Sylhet', 'DUMMY-NID-0003',
     TIMESTAMPTZ '2027-01-01 07:00:00+06', TIMESTAMPTZ '2027-01-01 12:00:00+06', 900.00),
    ('DHAKA-METRO-B-1002', 'Sylhet', 'Dhaka', 'DUMMY-NID-0004',
     TIMESTAMPTZ '2027-01-01 17:00:00+06', TIMESTAMPTZ '2027-01-01 22:00:00+06', 900.00),
    ('DHAKA-METRO-B-1002', 'Dhaka', 'Sylhet', 'DUMMY-NID-0003',
     TIMESTAMPTZ '2027-01-03 07:00:00+06', TIMESTAMPTZ '2027-01-03 12:00:00+06', 900.00),
    ('DHAKA-METRO-B-1002', 'Sylhet', 'Dhaka', 'DUMMY-NID-0004',
     TIMESTAMPTZ '2027-01-03 17:00:00+06', TIMESTAMPTZ '2027-01-03 22:00:00+06', 900.00),

    ('DHAKA-METRO-B-1003', 'Dhaka', 'Rajshahi', 'DUMMY-NID-0005',
     TIMESTAMPTZ '2027-01-01 08:00:00+06', TIMESTAMPTZ '2027-01-01 13:30:00+06', 1000.00),
    ('DHAKA-METRO-B-1003', 'Rajshahi', 'Dhaka', 'DUMMY-NID-0006',
     TIMESTAMPTZ '2027-01-01 18:00:00+06', TIMESTAMPTZ '2027-01-01 23:30:00+06', 1000.00),
    ('DHAKA-METRO-B-1003', 'Dhaka', 'Rajshahi', 'DUMMY-NID-0005',
     TIMESTAMPTZ '2027-01-03 08:00:00+06', TIMESTAMPTZ '2027-01-03 13:30:00+06', 1000.00),
    ('DHAKA-METRO-B-1003', 'Rajshahi', 'Dhaka', 'DUMMY-NID-0006',
     TIMESTAMPTZ '2027-01-03 18:00:00+06', TIMESTAMPTZ '2027-01-03 23:30:00+06', 1000.00)
) AS trip_data (
    bus_number, origin, destination, nid_number,
    departure_time, arrival_time, fare
)
JOIN vehicle ON vehicle.bus_number = trip_data.bus_number
JOIN route ON route.origin = trip_data.origin
          AND route.destination = trip_data.destination
JOIN driver ON driver.nid_number = trip_data.nid_number;
