# Hotel Booking System --- Project Plan

## 1. Project Overview

Build a basic hotel booking platform using **Java, Spring Boot, Spring
Data JPA, and PostgreSQL hosted on Supabase**.

The system will support two user roles:

-   **USER** --- registers, logs in, searches hotels/rooms, checks
    availability, books rooms, makes payments, and views their bookings.
-   **ADMIN** --- manages the hotel inventory by creating/updating
    hotels and creating/updating rooms inside those hotels.

The initial goal is to implement a clean backend structure first.
Advanced features can be added after the core workflow is working.

------------------------------------------------------------------------

## 2. Main Technology Stack

### Backend

-   Java 21
-   Spring Boot
-   Spring Web
-   Spring Data JPA / Hibernate
-   Spring Security
-   Bean Validation
-   Maven
-   Lombok

### Database

-   PostgreSQL
-   Supabase-hosted PostgreSQL

### Authentication

-   Spring Security
-   BCrypt password hashing
-   JWT authentication

### Email

-   SendGrid or a similar transactional email provider

### Development Tools

-   IntelliJ IDEA --- main Java/Spring development IDE
-   VS Code --- AI-assisted coding / Codex support
-   Postman --- REST API testing
-   Git/GitHub --- version control

------------------------------------------------------------------------

## 3. Core Functional Requirements

### Account

1.  A new user can create an account.
2.  Existing users can log in.
3.  Passwords must never be stored as plain text.
4.  Email must be unique.
5.  Users have a role: `USER` or `ADMIN`.
6.  After successful account creation, an email should be sent to the
    registered email address.

### Admin

An ADMIN can: - Create hotels. - Update hotel information. - Create
rooms inside a hotel. - Update room information. - Deactivate/remove
rooms when required. - View booking information.

Admin-created hotel data is stored in the `hotels` table.

Admin-created room data is stored in the `rooms` table, with each room
referencing its hotel through `hotel_id`.

Once created, these hotels and rooms become searchable/bookable by
normal users, subject to availability.

### Hotel Search

Users should eventually be able to filter/search using: - City -
Check-in date - Check-out date - Room capacity - Room category - Price

### Rooms

Rooms have: - Room number - Hotel - Category - Capacity - Price

Initial categories: - `NORMAL` - `DELUXE`

Example capacities: - 2 people - 3 people - 4 people

### Booking

A booking contains: - User - Room - Start/check-in date - End/check-out
date - Booking status - Creation time

Initial booking statuses: - `PENDING` - `CONFIRMED` - `CANCELLED` -
`COMPLETED`

### Payment

A payment contains: - Payment ID - Booking - Total amount - Paid
amount - Remaining amount - Payment method - Payment status -
Transaction ID - Creation time

Initial payment methods: - `CARD` - `BKASH` - `CASH`

Initial payment statuses: - `PENDING` - `PARTIALLY_PAID` - `PAID` -
`FAILED` - `REFUNDED`

When payment successfully confirms the booking, a booking confirmation
email should be sent.

------------------------------------------------------------------------

## 4. Important Availability Rule

Do **not** treat a room as permanently `AVAILABLE` or `BOOKED`.

Availability depends on the requested date range.

Example:

Room 101 has a confirmed booking:

`2026-10-10 -> 2026-10-13`

The same room may be: - unavailable for October 11, - but available for
October 20.

Therefore, availability must be calculated from bookings.

A room is available for a requested date range only when there is no
conflicting active booking for that room.

A room-level operational status may later be used for states such as: -
`ACTIVE` - `MAINTENANCE` - `OUT_OF_SERVICE`

This is different from booking availability.

------------------------------------------------------------------------

## 5. Concurrency Requirement

A critical requirement is:

> Two users must never be able to successfully reserve the same room for
> overlapping dates.

A simple frontend check is NOT sufficient.

The final implementation should use: - Spring transactions -
PostgreSQL/database-level concurrency protection - Availability checks -
A database constraint/strategy preventing overlapping active
reservations

The database should be treated as the final authority.

Concurrency implementation should be added only after basic booking CRUD
and availability logic are working.

------------------------------------------------------------------------

## 6. Initial Database Design

### USERS

  Column       Type / Purpose
  ------------ ---------------------
  id           BIGINT, Primary Key
  name         VARCHAR
  age          INTEGER
  email        VARCHAR, UNIQUE
  password     VARCHAR, hashed
  role         USER / ADMIN
  created_at   TIMESTAMP

Primary Key: `users.id`

Important: - Never store plain-text passwords. - Email should be unique.

------------------------------------------------------------------------

### HOTELS

  Column        Type / Purpose
  ------------- ---------------------
  id            BIGINT, Primary Key
  name          VARCHAR
  city          VARCHAR
  address       VARCHAR
  description   TEXT

Primary Key: `hotels.id`

------------------------------------------------------------------------

### ROOMS

  Column        Type / Purpose
  ------------- ---------------------
  id            BIGINT, Primary Key
  room_number   VARCHAR
  hotel_id      BIGINT, Foreign Key
  category      NORMAL / DELUXE
  capacity      INTEGER
  price         DECIMAL

Primary Key: `rooms.id`

Foreign Key: `rooms.hotel_id -> hotels.id`

Relationship: `HOTEL 1 ---- N ROOM`

A hotel can contain many rooms.

A room belongs to one hotel.

------------------------------------------------------------------------

### BOOKINGS

  Column       Type / Purpose
  ------------ ---------------------
  id           BIGINT, Primary Key
  user_id      BIGINT, Foreign Key
  room_id      BIGINT, Foreign Key
  start_date   DATE
  end_date     DATE
  status       Booking status
  created_at   TIMESTAMP

Primary Key: `bookings.id`

Foreign Keys: - `bookings.user_id -> users.id` -
`bookings.room_id -> rooms.id`

Relationships: - `USER 1 ---- N BOOKING` - `ROOM 1 ---- N BOOKING`

Do NOT duplicate: - user name - hotel name - hotel ID - room
information - payment method - payment due

Those values can be reached through the proper entity relationships.

Hotel information for a booking can be obtained through:

`Booking -> Room -> Hotel`

------------------------------------------------------------------------

### PAYMENTS

  Column             Type / Purpose
  ------------------ -----------------------------
  id                 BIGINT, Primary Key
  booking_id         BIGINT, Foreign Key, UNIQUE
  amount             DECIMAL
  paid_amount        DECIMAL
  remaining_amount   DECIMAL
  method             CARD / BKASH / CASH
  status             Payment status
  transaction_id     VARCHAR
  created_at         TIMESTAMP

Primary Key: `payments.id`

Foreign Key: `payments.booking_id -> bookings.id`

Initial relationship: `BOOKING 1 ---- 0..1 PAYMENT`

For the basic project, treat Booking and Payment as a one-to-one
relationship.

------------------------------------------------------------------------

## 7. Relationship Diagram

``` text
USER
│
│ 1
│
│ N
▼
BOOKING ───────────── PAYMENT
   │       1        0..1
   │
   │ N
   │
   │ 1
   ▼
 ROOM
   │
   │ N
   │
   │ 1
   ▼
 HOTEL
```

More explicitly:

``` text
USER 1 -------- N BOOKING
ROOM 1 -------- N BOOKING
HOTEL 1 ------- N ROOM
BOOKING 1 ----- 0..1 PAYMENT
```

------------------------------------------------------------------------

## 8. Basic Application Workflow

### Registration

``` text
User submits registration
        ↓
Validate information
        ↓
Check unique email
        ↓
Hash password
        ↓
Save USER
        ↓
Send account creation email
```

### Admin Inventory

``` text
ADMIN logs in
        ↓
Creates Hotel
        ↓
Hotel saved to HOTELS
        ↓
Creates rooms under Hotel
        ↓
Rooms saved to ROOMS with hotel_id
        ↓
Rooms become searchable by users
```

### User Search

``` text
USER logs in
      ↓
Selects city
      ↓
Selects check-in/check-out
      ↓
Optional capacity/category/price filters
      ↓
Backend searches hotels/rooms
      ↓
Backend removes rooms with conflicting bookings
      ↓
Available rooms returned
```

### Booking

``` text
USER selects room
      ↓
Backend validates dates
      ↓
Check room availability
      ↓
Concurrency protection
      ↓
Create PENDING booking
      ↓
Payment
      ↓
Payment successful
      ↓
Booking becomes CONFIRMED
      ↓
Send booking confirmation email
```

------------------------------------------------------------------------

# 9. Development Phases

## Phase 1 --- Project & Database Setup

Goal: Create a working Spring Boot application connected to Supabase
PostgreSQL.

Tasks: 1. Generate project with Spring Initializr. 2. Use Maven. 3. Use
Java 21. 4. Add initial dependencies: - Spring Web - Spring Data JPA -
PostgreSQL Driver - Spring Security - Validation - Lombok 5. Open
project in IntelliJ IDEA. 6. Create Supabase project. 7. Obtain
PostgreSQL connection information. 8. Configure Spring datasource using
environment variables/configuration. 9. Test that Spring Boot can
connect successfully. 10. Optionally connect IntelliJ's database viewer
to PostgreSQL for development inspection.

**Completion condition:** Spring Boot starts successfully and can
communicate with the Supabase PostgreSQL database.

------------------------------------------------------------------------

## Phase 2 --- JPA Entity Model

Goal: Represent the database structure using Java/JPA.

Create: - `User` - `Hotel` - `Room` - `Booking` - `Payment`

Create enums: - `UserRole` - `RoomCategory` - `BookingStatus` -
`PaymentMethod` - `PaymentStatus`

Implement relationships: - User -\> Bookings - Hotel -\> Rooms - Room
-\> Bookings - Booking -\> Payment

Add: - Primary keys - Foreign keys - JPA annotations - Required
constraints - Basic validation

**Completion condition:** JPA can create/map the initial schema
correctly.

------------------------------------------------------------------------

## Phase 3 --- Repository & Basic CRUD

Create: - `UserRepository` - `HotelRepository` - `RoomRepository` -
`BookingRepository` - `PaymentRepository`

Implement basic service/controller structure.

Initial APIs should support: - Creating test users - Creating hotels -
Creating rooms - Getting hotels - Getting rooms

At this stage, sample data may be manually inserted or created through
temporary/test endpoints.

**Completion condition:** Core entities can be saved and retrieved from
Supabase through Spring Data JPA.

------------------------------------------------------------------------

## Phase 4 --- Authentication & Authorization

Implement: - Registration - Login - BCrypt password hashing - JWT
generation - JWT validation - Spring Security configuration - Role-based
authorization

Roles: - `USER` - `ADMIN`

ADMIN-only operations must be protected.

Example: - USER cannot call hotel creation endpoints. - ADMIN can
create/manage hotel inventory.

**Completion condition:** Users can register/login and protected
endpoints correctly enforce roles.

------------------------------------------------------------------------

## Phase 5 --- Admin Hotel & Room Management

Implement ADMIN APIs for: - Create hotel - Update hotel - Get hotel -
Create room inside hotel - Update room - Deactivate/remove room

Example:

``` text
POST /admin/hotels
POST /admin/hotels/{hotelId}/rooms
PUT  /admin/hotels/{hotelId}
PUT  /admin/rooms/{roomId}
```

The exact endpoint design may change during implementation.

**Completion condition:** An ADMIN can populate hotel and room inventory
that normal users can retrieve.

------------------------------------------------------------------------

## Phase 6 --- Hotel Search & Room Availability

Implement filters for: - City - Capacity - Category - Price -
Check-in/check-out dates

Example conceptual request:

``` text
GET /hotels/search
    ?city=Dhaka
    &checkIn=2026-10-05
    &checkOut=2026-10-08
    &capacity=3
    &category=DELUXE
```

Availability must be based on bookings, NOT a simple permanent
`room.status = AVAILABLE`.

**Completion condition:** Search returns rooms matching filters that are
free for the requested dates.

------------------------------------------------------------------------

## Phase 7 --- Booking System

Implement: - Booking request - Date validation - User/room association -
Availability validation - `PENDING` booking creation - Booking
retrieval - Cancellation - Confirmation workflow

Important validation: - Check-out must be after check-in. - Room must
exist. - Room must be operational. - Booking dates must not conflict.

**Completion condition:** A user can create and manage valid bookings.

------------------------------------------------------------------------

## Phase 8 --- Concurrency Protection

Goal: Guarantee that overlapping bookings cannot both succeed for the
same room.

Test scenario:

``` text
User A ──┐
         ├── attempts Room 101 for same dates
User B ──┘
```

Expected result:

``` text
ONE booking succeeds
ONE booking is rejected
```

Use: - `@Transactional` - PostgreSQL concurrency mechanisms -
Appropriate database-level protection/constraint - Integration tests
simulating simultaneous booking attempts

Do not rely solely on: `if (roomIsAvailable)`

because two concurrent requests could both pass that check before either
transaction finishes.

**Completion condition:** Concurrent conflicting reservation requests
cannot cause a double booking.

------------------------------------------------------------------------

## Phase 9 --- Payment System

Implement payment records containing: - Amount - Paid amount - Remaining
amount - Method - Status - Transaction ID

Methods: - CARD - BKASH - CASH

Initial payment integration may be simulated before integrating any real
payment gateway.

Booking should become `CONFIRMED` only according to the chosen
payment/business rules.

**Completion condition:** Payment information is correctly associated
with bookings and confirmation state works.

------------------------------------------------------------------------

## Phase 10 --- Email System

Use SendGrid or similar technology.

Send email when: 1. Account registration succeeds. 2. Booking/payment
confirmation succeeds.

Create an abstraction such as:

``` text
EmailService
```

So the rest of the application does not depend directly on
SendGrid-specific code.

Later consider asynchronous email processing so API requests do not have
to wait for the email provider.

**Completion condition:** Registration and confirmed-booking emails are
successfully sent.

------------------------------------------------------------------------

## Phase 11 --- Validation & Exception Handling

Implement centralized error handling.

Handle cases such as: - Duplicate email - Invalid login - Unauthorized
role - Hotel not found - Room not found - Invalid dates - Room
unavailable - Booking conflict - Payment failure - Invalid payment
amount - Invalid capacity/category - Invalid request body

Use proper HTTP status codes and consistent error responses.

------------------------------------------------------------------------

## Phase 12 --- Testing & Production Preparation

Testing: - Unit tests - Repository tests - Service tests -
Controller/API tests - Authentication tests - Booking tests -
Concurrency tests - Payment tests

Production preparation: - Environment variables - Do not commit
passwords/API keys - Database credentials secured - SendGrid API key
secured - Production PostgreSQL configuration - CORS configuration -
Logging - Deployment configuration - HTTPS - Build production JAR

------------------------------------------------------------------------

# 10. Suggested Package Structure

The exact structure can evolve, but begin with a layered structure
similar to:

``` text
src/main/java/com/hotelbooking/

├── config/
├── controller/
├── dto/
│   ├── request/
│   └── response/
├── entity/
├── enums/
├── exception/
├── repository/
├── security/
├── service/
└── HotelBookingApplication.java
```

Possible entities:

``` text
entity/
├── User.java
├── Hotel.java
├── Room.java
├── Booking.java
└── Payment.java
```

Possible enums:

``` text
enums/
├── UserRole.java
├── RoomCategory.java
├── BookingStatus.java
├── PaymentMethod.java
└── PaymentStatus.java
```

Do not create every package/class prematurely. Add them as the
corresponding phase begins.

------------------------------------------------------------------------

# 11. Important Design Rules for Codex

When assisting with this repository, follow these rules:

1.  Do not implement future phases unless explicitly requested.
2.  Prefer simple, understandable code suitable for a university/basic
    hotel booking project.
3.  Use Java 21 and Spring Boot.
4.  Use Maven.
5.  Use Spring Data JPA for persistence.
6.  Use PostgreSQL/Supabase as the database.
7.  Do not store plain-text passwords.
8.  Keep `USER` and `ADMIN` roles.
9.  Only ADMIN users may manage hotels and rooms.
10. Do not store duplicate hotel/user/room details in `Booking` when
    relationships already provide them.
11. Do not determine booking availability using only a permanent
    `room.status`.
12. Availability must be date-range based.
13. Prevent overlapping bookings at the database/transaction level, not
    only through frontend checks.
14. Use `BigDecimal` for monetary values in Java rather than
    `double`/`float`.
15. Use proper DTOs instead of exposing JPA entities directly from
    production-facing controllers.
16. Keep controllers thin; business logic belongs in services.
17. Repositories should handle persistence/query operations.
18. Validate all user input.
19. Never hard-code database passwords, JWT secrets, SendGrid keys, or
    other credentials.
20. Explain significant architectural changes before making them.
21. Avoid unnecessary complexity and premature microservices.
22. The initial application should remain a single Spring Boot
    application.
23. Write code incrementally and keep the application runnable after
    each step.

------------------------------------------------------------------------

# 12. Current Status

Current phase:

**PHASE 1 --- Project & Database Setup**

Current immediate task:

Create the Spring Boot project using Spring Initializr.

Configuration:

``` text
Project: Maven
Language: Java
Java: 21
Packaging: Jar

Group: com.hotelbooking
Artifact: hotel-booking
Name: hotel-booking
Package: com.hotelbooking
```

Initial dependencies:

``` text
Spring Web
Spring Data JPA
PostgreSQL Driver
Spring Security
Validation
Lombok
```

After generation:

1.  Extract the project.
2.  Open it in IntelliJ IDEA.
3.  Allow Maven dependencies to load.
4.  Do not start implementing entities yet.
5.  Next task is creating the Supabase project and connecting Spring
    Boot to PostgreSQL.

------------------------------------------------------------------------

# 13. Future Improvement Ideas --- Not Part of Initial Build

Do not implement these until the basic project works:

-   Separate `RoomType` entity
-   Hotel images
-   Room images
-   Amenities
-   Reviews/ratings
-   Multiple hotel administrators
-   Hotel ownership
-   Discount/coupon system
-   Refund system
-   Real payment gateway
-   Booking invoices
-   Email verification tokens
-   Password reset
-   Refresh tokens
-   Audit logs
-   Pagination
-   Advanced sorting
-   Favorites/wishlist
-   Multi-room booking
-   Dynamic pricing

A future normalized model could separate physical rooms from room types,
e.g.:

``` text
HOTEL
  ↓
ROOM_TYPE
  ↓
ROOM
```

But for the first implementation, keeping `category`, `capacity`, and
`price` directly in `Room` is intentionally simpler.

------------------------------------------------------------------------

# 14. Definition of Initial Success

The first complete version is successful when:

1.  USER can register and login.
2.  Registration email can be sent.
3.  ADMIN can create hotels.
4.  ADMIN can create rooms under hotels.
5.  USER can search hotels/rooms.
6.  USER can filter by city/capacity/category/dates.
7.  Availability is calculated correctly.
8.  USER can create a booking.
9.  Concurrent users cannot double-book the same room for overlapping
    dates.
10. Payment information can be stored.
11. Successful payment can confirm a booking.
12. Booking confirmation email can be sent.
13. Data persists correctly in Supabase PostgreSQL.
