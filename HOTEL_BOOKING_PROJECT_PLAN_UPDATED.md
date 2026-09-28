# Hotel Booking System --- Project Plan

## 1. Project Overview

This project is a backend Hotel Booking System built with Java, Spring
Boot, Spring Data JPA/Hibernate, and PostgreSQL hosted on Supabase.

The current scope focuses on normal users. Hotels and rooms are
populated manually in the database. Admin functionality is intentionally
deferred to future scope.

### Technology Stack

-   Java 21
-   Spring Boot 4.1.1
-   Spring Web / WebMVC
-   Spring Data JPA / Hibernate
-   PostgreSQL
-   Supabase
-   Maven
-   Lombok
-   Jakarta Validation
-   Manual JWT authentication
-   BCrypt password hashing
-   JJWT
-   IntelliJ IDEA

### Important Authentication Decision

This project does **not** use the Spring Security framework.

Authentication is implemented manually using:

-   JWT
-   A Jakarta Servlet filter
-   BCrypt password verification
-   An authenticated-user request attribute

Do not reintroduce Spring Security unless the architecture is
intentionally redesigned later.

------------------------------------------------------------------------

# 2. Core Requirements

The system should allow a normal user to:

1.  Register an account.
2.  Log in and receive a JWT.
3.  Browse hotels.
4.  Search hotels by city/name.
5.  Browse and filter rooms.
6.  Search for rooms available during a requested date range.
7.  Create a booking.
8.  View their own bookings.
9.  Cancel their own bookings.
10. Make/record payment.
11. Receive booking confirmation after successful payment.
12. Receive a confirmation email.

The system must prevent overlapping active bookings for the same room.

------------------------------------------------------------------------

# 3. Database Model

## User

Main fields:

-   id
-   name
-   age
-   email
-   password
-   role
-   createdAt

Current public registration always creates a normal `USER`.

`ADMIN` may remain in the enum for future scope, but admin functionality
is not currently implemented.

## Hotel

Main fields:

-   id
-   name
-   city
-   address
-   description

Relationship:

`Hotel 1 ---- N Room`

Hotels are currently inserted directly into Supabase.

## Room

Main fields:

-   id
-   roomNumber
-   hotel
-   category
-   capacity
-   price

Room categories:

-   NORMAL
-   DELUXE

Room number should be unique within a hotel.

A room does **not** have a permanent `AVAILABLE/BOOKED` status.

Availability is calculated from bookings and requested dates.

## Booking

Main fields:

-   id
-   user
-   room
-   startDate
-   endDate
-   status
-   createdAt

Booking statuses:

-   PENDING
-   CONFIRMED
-   CANCELLED
-   COMPLETED

Relationships:

`User 1 ---- N Booking`

`Room 1 ---- N Booking`

## Payment

Main fields:

-   id
-   booking
-   amount
-   paidAmount
-   remainingAmount
-   method
-   status
-   transactionId
-   createdAt

Payment methods:

-   CARD
-   CASH
-   BKASH

Payment statuses:

-   PENDING
-   PARTIALLY_PAID
-   PAID
-   FAILED
-   REFUNDED

Relationship:

`Booking 1 ---- 0..1 Payment`

------------------------------------------------------------------------

# 4. Date and Availability Rules

Booking date intervals use:

`[startDate, endDate)`

This means the start date is inclusive and the end date is exclusive.

Example:

-   Existing booking: October 10 → October 15
-   New booking: October 15 → October 20

These do **not** overlap.

Two bookings overlap when:

``` text
existing.startDate < requestedCheckOut
AND
existing.endDate > requestedCheckIn
```

Statuses that block availability:

-   PENDING
-   CONFIRMED

Statuses that do not block availability:

-   CANCELLED
-   COMPLETED

Availability must be calculated from the Booking table rather than
storing a permanent booked/available value on Room.

------------------------------------------------------------------------

# 5. API Response Standard

All REST APIs should use one generic response structure.

``` json
{
  "responseCode": 200,
  "responseMessage": "Request successful",
  "data": {}
}
```

The top-level fields are always:

1.  `responseCode`
2.  `responseMessage`
3.  `data`

Recommended generic DTO:

``` java
public record ApiResponse<T>(
        int responseCode,
        String responseMessage,
        T data
) {}
```

The actual HTTP status and `responseCode` must match.

Example successful creation:

``` json
{
  "responseCode": 201,
  "responseMessage": "Booking created successfully",
  "data": {
    "bookingReference": "7a9d71cb-3b74-4f53-91e8-5c3f2e04e3a1",
    "status": "PENDING"
  }
}
```

Example error:

``` json
{
  "responseCode": 409,
  "responseMessage": "Room is not available for the selected dates",
  "data": null
}
```

Validation errors, JWT errors, exception-handler responses, and normal
controller responses should all follow this format.

------------------------------------------------------------------------

# 6. Development Phases

## Phase 1 --- Project + Supabase Setup ✅ COMPLETE

Implemented:

-   Spring Boot project
-   Java 21
-   Maven
-   Supabase PostgreSQL connection
-   Environment-variable based datasource configuration
-   JPA/Hibernate configuration

Important environment variables:

``` text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
```

Database credentials and JWT secrets must not be hardcoded into source
control.

------------------------------------------------------------------------

## Phase 2 --- JPA Entities + Relationships ✅ COMPLETE

Implemented:

-   User
-   Hotel
-   Room
-   Booking
-   Payment

Implemented enums:

-   UserRole
-   RoomCategory
-   BookingStatus
-   PaymentMethod
-   PaymentStatus

Implemented relationships:

``` text
USER    1 ---- N BOOKING
HOTEL   1 ---- N ROOM
ROOM    1 ---- N BOOKING
BOOKING 1 ---- 0..1 PAYMENT
```

Money values use `BigDecimal`.

------------------------------------------------------------------------

## Phase 3 --- Repository / Data Access Layer ✅ COMPLETE

Implemented Spring Data JPA repositories for:

-   User
-   Hotel
-   Room
-   Booking
-   Payment

Repositories contain methods required by authentication, searching, room
filtering, booking ownership, and availability.

------------------------------------------------------------------------

## Phase 4 --- Manual JWT Authentication ✅ COMPLETE

Originally explored with Spring Security, then intentionally refactored
to manual authentication for learning.

Current architecture:

``` text
Request
   ↓
Manual JWT Servlet Filter
   ↓
Validate Bearer JWT
   ↓
Load authenticated user
   ↓
request.setAttribute("authenticatedUser", ...)
   ↓
Protected Controller
```

Implemented:

-   Registration
-   Login
-   BCrypt password hashing/checking
-   JWT generation
-   JWT validation
-   Manual authentication filter
-   Current-user endpoint
-   Public/protected route handling

Important rule:

Controllers should not parse JWTs themselves.

The manual filter establishes the authenticated identity.

------------------------------------------------------------------------

## Phase 5 --- Hotel & Room Browsing/Search ✅ COMPLETE

Implemented public hotel APIs.

Examples:

``` text
GET /api/hotels
GET /api/hotels/{hotelId}
GET /api/hotels/search
```

Hotel search supports:

-   city
-   partial name

Implemented room APIs.

Examples:

``` text
GET /api/hotels/{hotelId}/rooms
GET /api/rooms/{roomId}
```

Room filtering supports:

-   capacity
-   category
-   minimum price
-   maximum price

Filtering is performed at the database/repository layer.

Hotels and rooms are currently populated manually in Supabase.

------------------------------------------------------------------------

## Phase 6 --- Date-Based Room Availability ✅ COMPLETE

Implemented:

``` text
GET /api/hotels/{hotelId}/rooms/available
```

Required parameters:

-   checkIn
-   checkOut

Optional filters:

-   capacity
-   category
-   minPrice
-   maxPrice

Availability is calculated in PostgreSQL using a `NOT EXISTS` query
against overlapping active bookings.

Core overlap rule:

``` text
booking.startDate < checkOut
AND
booking.endDate > checkIn
```

Only `PENDING` and `CONFIRMED` bookings block availability.

The endpoint is public.

No booking is created by an availability search.

------------------------------------------------------------------------

## Phase 7 --- Booking System ✅ COMPLETE

Implemented:

``` text
POST  /api/bookings
GET   /api/bookings/my
POST  /api/bookings/details
PATCH /api/bookings/cancel
```

All booking endpoints require authentication.

### Creating a Booking

Example request:

``` json
{
  "roomId": 3,
  "checkIn": "2026-11-10",
  "checkOut": "2026-11-15"
}
```

The request does **not** accept a user ID.

The authenticated user comes from:

``` text
request.getAttribute("authenticatedUser")
```

New bookings are created with:

``` text
status = PENDING
```

Before insertion, availability is checked using the same overlap rules
as Phase 6.

An unavailable room returns:

``` text
409 Conflict
```

### Ownership Protection

Booking ownership queries use both the public booking UUID reference and
authenticated user ID. Internal database IDs remain server-side only.

A user cannot view or cancel another user's booking.

A nonexistent or foreign-owned booking returns `404`.

### Cancellation

Allowed:

``` text
PENDING   → CANCELLED
CONFIRMED → CANCELLED
```

Rejected:

``` text
CANCELLED → CANCELLED
COMPLETED → CANCELLED
```

Cancelled bookings are retained in the database rather than deleted.

------------------------------------------------------------------------

## Phase 8 --- Concurrency / Double-Booking Protection ⬜ NEXT

Goal:

Guarantee that two simultaneous requests cannot create overlapping
active bookings for the same room.

Two protection layers will be used.

### Layer 1 --- Application Check

Spring checks availability before inserting the booking.

This provides a clean normal response when the room is already
unavailable.

### Layer 2 --- PostgreSQL Final Guarantee

PostgreSQL will enforce a database-level exclusion constraint.

Conceptually:

``` sql
EXCLUDE USING gist (
    room_id WITH =,
    daterange(start_date, end_date, '[)') WITH &&
)
WHERE (status IN ('PENDING', 'CONFIRMED'))
```

`btree_gist` may be required for equality comparison on `room_id`.

Booking creation should run inside a Spring transaction:

``` java
@Transactional
```

and use an appropriate flush strategy so the database violation occurs
within the operation.

If two requests race:

``` text
Request A → availability passes → INSERT → succeeds
Request B → availability passes → INSERT → PostgreSQL rejects overlap
```

The specific overlap constraint violation should be translated to:

``` text
409 Conflict
```

Do not broadly translate every database error into room-unavailable.

The database is the final authority.

------------------------------------------------------------------------

## API Response Standardization ⬜ TO APPLY

Before or alongside the next backend work, standardize all current REST
responses using:

``` text
ApiResponse<T>
```

Required format:

``` json
{
  "responseCode": 200,
  "responseMessage": "Request successful",
  "data": {}
}
```

Apply it to:

-   Authentication
-   Current user
-   Hotels
-   Rooms
-   Availability
-   Bookings
-   Validation errors
-   Global exception handling
-   Manual JWT filter 401 responses
-   Future payment APIs

Do not change existing business logic while performing this refactor.

------------------------------------------------------------------------

## Phase 9 --- Payment System ⬜ PENDING

Implement payment functionality related to a booking.

Payment should contain:

-   booking
-   amount
-   paidAmount
-   remainingAmount
-   payment method
-   payment status
-   transaction ID
-   creation timestamp

Methods:

-   CARD
-   CASH
-   BKASH

Statuses:

-   PENDING
-   PARTIALLY_PAID
-   PAID
-   FAILED
-   REFUNDED

Current intended confirmation rule:

``` text
Booking created
      ↓
PENDING
      ↓
Required payment completed successfully
      ↓
Payment = PAID
Booking = CONFIRMED
```

Payment APIs must use the generic `ApiResponse<T>` structure.

------------------------------------------------------------------------

## Phase 10 --- Email System ⬜ PENDING

Implement automated email notifications.

Primary flow:

``` text
Successful required payment
        ↓
Booking becomes CONFIRMED
        ↓
Send booking confirmation email
```

Email may use SendGrid or another suitable provider.

Registration email may also be implemented here if still required.

Email failure should be handled carefully so that an already successful
database/payment operation is not incorrectly duplicated.

------------------------------------------------------------------------

## Phase 11 --- Validation + Exception Handling ⬜ PENDING

Most core validation already exists, but this phase will review and
standardize it.

Review:

-   invalid request bodies
-   missing fields
-   invalid dates
-   invalid enum values
-   nonexistent resources
-   unauthorized access
-   booking ownership
-   room conflicts
-   payment errors
-   unexpected errors

All API errors should use:

``` json
{
  "responseCode": 400,
  "responseMessage": "Useful error message",
  "data": null
}
```

No stack traces or internal database/JWT details should be returned to
clients.

------------------------------------------------------------------------

## Phase 12 --- Testing + Production Preparation ⬜ PENDING

Final backend verification.

Test complete flows:

``` text
Register
  ↓
Login
  ↓
Browse hotels
  ↓
Filter rooms
  ↓
Check availability
  ↓
Create booking
  ↓
Concurrency protection
  ↓
Payment
  ↓
Booking confirmation
  ↓
Email
```

Also review:

-   environment variables
-   database constraints
-   indexes
-   transaction boundaries
-   validation
-   API response consistency
-   authentication
-   ownership protection
-   error handling
-   Maven tests
-   production configuration
-   secrets management
-   deployment readiness

------------------------------------------------------------------------

# 7. Booking Lifecycle

The intended booking lifecycle is:

``` text
Room available
     ↓
User creates booking
     ↓
PENDING
     ↓
Payment completed successfully
     ↓
CONFIRMED
     ↓
Stay finishes
     ↓
COMPLETED
```

Alternative path:

``` text
PENDING / CONFIRMED
        ↓
User cancels
        ↓
CANCELLED
```

`PENDING` and `CONFIRMED` block room availability.

`CANCELLED` and `COMPLETED` do not.

------------------------------------------------------------------------

# 8. Security Rules

1.  Passwords must be stored as BCrypt hashes.
2.  JWT secret must come from environment configuration.
3.  Do not hardcode database credentials.
4.  Do not trust client-provided user IDs for protected user operations.
5.  Booking identity must come from the validated JWT.
6.  Users may access only their own bookings.
7.  Controllers should not duplicate JWT parsing.
8.  Do not expose password hashes.
9.  Do not expose internal exception details.
10. Do not reintroduce Spring Security into the current architecture.

------------------------------------------------------------------------

# 9. Concurrency Rule

Application-level availability checking alone is not sufficient.

Correct design:

``` text
Spring availability check
        +
@Transactional booking operation
        +
PostgreSQL overlap exclusion constraint
```

The PostgreSQL constraint is the final correctness guarantee.

This protects the system even if two requests reach different
application instances at the same time.

------------------------------------------------------------------------

# 10. Future Scope

The following are intentionally outside the current basic backend scope:

-   Admin dashboard
-   Admin hotel CRUD
-   Admin room CRUD
-   Hotel-owner accounts
-   Frontend application
-   Reviews and ratings
-   Advanced pricing
-   Coupons
-   Refund automation
-   OAuth/social login
-   Refresh tokens
-   Analytics/reporting

Hotels and rooms are currently managed directly in Supabase.

------------------------------------------------------------------------

# 11. Current Progress

``` text
Phase 1  Project + Supabase Setup                ✅
Phase 2  JPA Entities + Relationships            ✅
Phase 3  Repository Layer                        ✅
Phase 4  Manual JWT Authentication               ✅
Phase 5  Hotel & Room Browsing/Search            ✅
Phase 6  Date-Based Availability                 ✅
Phase 7  Booking System                          ✅
Phase 8  Concurrency Protection                  ⬜
Phase 9  Payment System                          ⬜
Phase 10 Email System                            ⬜
Phase 11 Validation + Exception Handling         ⬜
Phase 12 Testing + Production Preparation        ⬜

Additional:
Generic API Response Standardization             ⬜
```

------------------------------------------------------------------------

# 12. Immediate Next Work

1.  Complete the generic `ApiResponse<T>` response-standardization
    refactor.
2.  Complete Phase 8 concurrency protection.
3.  Verify the PostgreSQL overlap constraint in Supabase.
4.  Test simultaneous/conflicting bookings.
5.  Continue to Phase 9 payment implementation.

------------------------------------------------------------------------

# 13. Key Architectural Principle

The project should keep responsibilities separated:

``` text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Authentication:

``` text
Request
    ↓
Manual JWT Filter
    ↓
AuthenticatedUser
    ↓
Controller / Service
```

Room availability:

``` text
Requested dates
    ↓
Database overlap query
    ↓
Available rooms
```

Booking correctness:

``` text
Application availability check
    ↓
Transactional booking creation
    ↓
PostgreSQL concurrency constraint
```

This structure should be preserved as later phases are implemented.
