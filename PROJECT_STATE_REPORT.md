# Project State Report — Hotel-Management-System

Generated 2026-10-13 from the working tree at `E:\RedDOT\Hotel-Management-System`. Read-only inventory; no secret values are included.

### 1. GIT STATE

- Branch: `local` (no upstream configured). Other branches: `master`, `origin/master`.
- HEAD: `d1a8330` "Add logging for password reset token delivery failures in PasswordResetService and ResendPasswordResetTokenDelivery", committed 2026-10-04 16:30:59 +0600.
- Ahead/behind: `local` has no upstream. Against the locally cached `origin/master` it is 0 ahead and 0 behind, and `master` is also at `d1a8330`. No `git fetch` was run, so the remote may have moved.
- **Everything below is uncommitted. The GitHub repo shows only `d1a8330`.**

`git status --porcelain` (verbatim):
```
 M frontend/src/App.jsx
 M frontend/src/api/apiClient.js
 M frontend/src/api/authApi.js
 M frontend/src/components/Navbar.jsx
 M frontend/src/pages/SignupPage.jsx
 M frontend/src/styles/global.css
 M pom.xml
 M src/main/java/com/hotelbooking/hotel_booking/controller/AuthController.java
 M src/main/java/com/hotelbooking/hotel_booking/service/PaymentService.java
 M src/main/java/com/hotelbooking/hotel_booking/service/ResendPasswordResetTokenDelivery.java
 M src/main/resources/.env/local.example.json
 M src/main/resources/.env/prod.example.json
 M src/main/resources/api-success-messages.json
 M src/main/resources/application-local.yml
 M src/main/resources/application.yml
 M src/main/resources/exception-messages.json
 M src/main/resources/security/open_apis.json
 M src/test/java/com/hotelbooking/hotel_booking/database/ProductionDatabaseConfigurationTest.java
 M src/test/java/com/hotelbooking/hotel_booking/service/AuthServiceLoginProtectionTest.java
 M src/test/java/com/hotelbooking/hotel_booking/service/PaymentServiceTest.java
?? frontend/src/api/transportApi.js
?? frontend/src/pages/TransportBookingsPage.jsx
?? frontend/src/pages/TransportCheckoutPage.jsx
?? frontend/src/pages/TransportPage.jsx
?? src/main/java/com/hotelbooking/hotel_booking/config/CachingConfiguration.java
?? src/main/java/com/hotelbooking/hotel_booking/dto/BookingConfirmationEmailData.java
?? src/main/java/com/hotelbooking/hotel_booking/dto/ResendSignupOtpRequest.java
?? src/main/java/com/hotelbooking/hotel_booking/dto/VerifySignupRequest.java
?? src/main/java/com/hotelbooking/hotel_booking/entity/SignupOtpConfiguration.java
?? src/main/java/com/hotelbooking/hotel_booking/entity/SignupVerification.java
?? src/main/java/com/hotelbooking/hotel_booking/event/
?? src/main/java/com/hotelbooking/hotel_booking/repository/SignupOtpConfigurationRepository.java
?? src/main/java/com/hotelbooking/hotel_booking/repository/SignupVerificationRepository.java
?? src/main/java/com/hotelbooking/hotel_booking/service/BookingConfirmationEmailService.java
?? src/main/java/com/hotelbooking/hotel_booking/service/BookingConfirmedEmailListener.java
?? src/main/java/com/hotelbooking/hotel_booking/service/DevelopmentSignupOtpDelivery.java
?? src/main/java/com/hotelbooking/hotel_booking/service/NoOpSignupOtpDelivery.java
?? src/main/java/com/hotelbooking/hotel_booking/service/ResendBookingConfirmationEmailService.java
?? src/main/java/com/hotelbooking/hotel_booking/service/ResendEmailSender.java
?? src/main/java/com/hotelbooking/hotel_booking/service/ResendSignupOtpDelivery.java
?? src/main/java/com/hotelbooking/hotel_booking/service/SignupOtpConfigurationService.java
?? src/main/java/com/hotelbooking/hotel_booking/service/SignupOtpDelivery.java
?? src/main/java/com/hotelbooking/hotel_booking/service/SignupVerificationService.java
?? src/main/resources/db/migration/V5__create_signup_verifications.sql
?? src/main/resources/db/migration/V6__create_signup_otp_configuration.sql
?? src/test/java/com/hotelbooking/hotel_booking/config/CachingConfigurationTest.java
?? src/test/java/com/hotelbooking/hotel_booking/service/BookingConfirmedEmailListenerTest.java
?? src/test/java/com/hotelbooking/hotel_booking/service/SignupOtpConfigurationServiceTest.java
?? src/test/java/com/hotelbooking/hotel_booking/service/SignupVerificationServiceTest.java
?? src/test/resources/application-test.yml
?? transport-service/
```
(The `.env/local.json` and `.env/prod.json` files and `target/` are gitignored, so they don't appear above.)

**Per-file notes.** Abbreviations: `java/` = `src/main/java/com/hotelbooking/hotel_booking/`, `res/` = `src/main/resources/`, `test/` = `src/test/java/com/hotelbooking/hotel_booking/`.

Modified:
- `frontend/src/App.jsx`: adds routes `/transport`, `/transport/bookings`, `/transport/checkout/:bookingId`.
- `frontend/src/api/apiClient.js`: generic `request(baseUrl, …)`; adds `transportApiRequest` (base `VITE_TRANSPORT_API_BASE_URL`, default `http://localhost:8081`).
- `frontend/src/api/authApi.js`: adds `verifySignup`, `resendSignupOtp`.
- `frontend/src/components/Navbar.jsx`: adds "Transport" and "Transport Bookings" links.
- `frontend/src/pages/SignupPage.jsx`: after register, opens a 6-digit OTP entry with verify and resend.
- `frontend/src/styles/global.css`: styles for the OTP UI.
- `pom.xml`: adds `spring-boot-starter-data-redis` (no version).
- `java/controller/AuthController.java`: `/register` now starts OTP signup (200, no data, no user created); adds `/verify-signup` and `/resend-signup-otp`.
- `java/service/PaymentService.java`: publishes `BookingConfirmedEvent` after a successful payment confirms a booking.
- `java/service/ResendPasswordResetTokenDelivery.java`: HTTP code moved into the shared `ResendEmailSender`; adds a plain-text body.
- `res/.env/local.example.json`, `res/.env/prod.example.json`: add a placeholder `REDIS_URL`.
- `res/api-success-messages.json`: adds `auth.signup.otp.sent` and `auth.signup.verified`.
- `res/application-local.yml`: `ddl-auto` changed from `update` to `none`, `default_schema "Production"`, Hikari pool max 3.
- `res/application.yml`: Redis URL, exclusion of Boot's Redis auto-configuration, Redis health indicator disabled.
- `res/exception-messages.json`: six `signup.*` error keys.
- `res/security/open_apis.json`: opens `POST /api/auth/verify-signup` and `POST /api/auth/resend-signup-otp`.
- `test/database/ProductionDatabaseConfigurationTest.java`: asserts the new local settings (`none`, `"Production"`).
- `test/service/AuthServiceLoginProtectionTest.java`: lock assertion changed from ~15 min to ~60 s, to match the code's 1-minute lock.
- `test/service/PaymentServiceTest.java`: verifies the event is published on success and not on failure.

Untracked:
- `frontend/src/api/transportApi.js` and `pages/Transport*.jsx`: transport UI (trips, seats, bookings, payments) calling `transport-service`.
- `java/config/CachingConfiguration.java`: Redis or in-memory cache plus an error handler (section 10).
- `java/dto/BookingConfirmationEmailData.java`, `ResendSignupOtpRequest.java`, `VerifySignupRequest.java`: DTO records.
- `java/entity/SignupVerification.java`, `SignupOtpConfiguration.java`, and their repositories: pending signups and OTP validity configuration.
- `java/event/BookingConfirmedEvent.java`: event record carrying the email data.
- `java/service/BookingConfirmationEmailService.java`, `ResendBookingConfirmationEmailService.java`, `BookingConfirmedEmailListener.java`: confirmation email sent after commit.
- `java/service/ResendEmailSender.java`: shared Resend HTTP client.
- `java/service/SignupOtpDelivery.java` and its Development/NoOp/Resend implementations: OTP delivery strategies.
- `java/service/SignupVerificationService.java`: start, verify and resend for OTP signup.
- `java/service/SignupOtpConfigurationService.java`: cached OTP validity with a safe default.
- `res/db/migration/V5__…`, `V6__…`: signup tables (section 5).
- `test/config/CachingConfigurationTest.java` and `test/service/{BookingConfirmedEmailListener,SignupOtpConfigurationService,SignupVerificationService}Test.java`: new tests.
- `src/test/resources/application-test.yml`: sets `app.password-reset.delivery: noop` for tests.
- `transport-service/` (90 files excluding `target/`): a separate Spring Boot 4.1.1 app with its own pom, schema `transport`, Flyway V1–V5, port 8081, 3 Postman collections. **Not visible on GitHub.**

### 2. WHAT HAS BEEN BUILT SINCE COMMIT d1a8330 (2026-10-04)

Nothing has been committed since `d1a8330`. All of the following exists only in this working tree:

1. **Email-OTP signup (backend + frontend). Functionally complete; the local test passes.** `/register` now stores a pending `signup_verifications` row (bcrypt password hash and bcrypt OTP hash) and sends a 6-digit OTP. `/verify-signup` creates the user, with a 5-attempt limit. `/resend-signup-otp` has a 60 s cooldown. Migrations V5/V6 add the tables. **Half-finished for deploy:** in prod the OTP is only sent when `PASSWORD_RESET_DELIVERY=resend`. The prod default is `noop`, which silently discards the OTP, so nobody could complete a signup.
2. **Booking confirmation email. Complete.** `PaymentService` publishes an event; an after-commit listener sends a Resend email. A send failure is logged and never fails the payment. Active only when delivery is `resend`.
3. **Resend refactor. Complete.** A shared `ResendEmailSender` is used for password reset, signup OTP and booking emails.
4. **Local profile change. Complete, but inconsistent with the local secrets file (section 11.1).** Local now uses `ddl-auto: none` and schema `"Production"`, with Flyway off.
5. **Redis/Upstash caching of the OTP validity. Complete and unit/integration tested; never run against a real Upstash.** See section 10.
6. **Login-lock test aligned to the 1-minute lock** in the code. The committed `AuthService` at HEAD already locks for 1 minute, while HEAD's committed test expects ~15 minutes.
7. **transport-service + transport frontend pages. Large, and status unknown.** I did not build or test `transport-service` for this report. It depends on the hotel API (`/api/user/me`) for identity and role.

### 3. BUILD AND TEST STATUS

`./mvnw -B test` (root project only): **Tests run: 81, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS.** No failing tests.
`transport-service` tests were not run.

### 4. FILE INVENTORY

`src/main/java/com/hotelbooking/hotel_booking/`:
```
HotelBookingApplication.java [committed]
config/: .gitkeep, CorsConfiguration.java, JsonEnvironmentPostProcessor.java [committed]; CachingConfiguration.java [untracked]
controller/: .gitkeep, BookingController, HotelController, PaymentController, RoomController, UserController [committed]; AuthController [modified]
dto/: .gitkeep, ApiResponse, AuthResponse, BookingResponse, CreateBookingRequest, CurrentUserResponse, ForgotPasswordRequest,
      HotelResponse, LoginRequest, PaymentResponse, PaymentSubmissionRequest, PaymentSubmissionResult, RegisterRequest,
      ResetPasswordRequest, RoomResponse [committed]; BookingConfirmationEmailData, ResendSignupOtpRequest, VerifySignupRequest [untracked]
entity/: Booking, Hotel, PasswordResetToken, Payment, Room, User [committed]; SignupOtpConfiguration, SignupVerification [untracked]
enums/: BookingStatus, PaymentMethod, PaymentStatus, RoomCategory, UserRole [committed]
event/: BookingConfirmedEvent [untracked]
exception/: .gitkeep, ApiException, ExceptionMessageCatalog, GlobalExceptionHandler [committed]
repository/: .gitkeep, BookingRepository, HotelRepository, PasswordResetTokenRepository, PaymentRepository, RoomRepository,
      UserRepository [committed]; SignupOtpConfigurationRepository, SignupVerificationRepository [untracked]
security/: AuthenticatedUser, AuthenticatedUserContext, JwtAuthenticationFilter, JwtService, OpenApiRegistry [committed]
service/: .gitkeep, ApiSuccessMessageCatalog, AuthService, BookingService, DateRangeValidator, DevelopmentPasswordResetTokenDelivery,
      DummyPaymentProcessor, HotelService, NoOpPasswordResetTokenDelivery, PasswordResetService, PasswordResetTokenDelivery,
      PaymentProcessor, RoomService [committed]; PaymentService, ResendPasswordResetTokenDelivery [modified];
      BookingConfirmationEmailService, BookingConfirmedEmailListener, DevelopmentSignupOtpDelivery, NoOpSignupOtpDelivery,
      ResendBookingConfirmationEmailService, ResendEmailSender, ResendSignupOtpDelivery, SignupOtpConfigurationService,
      SignupOtpDelivery, SignupVerificationService [untracked]
```
`src/test/java/com/hotelbooking/hotel_booking/`:
```
HotelBookingApplicationTests [committed]
config/: JsonEnvironmentPostProcessorTest, LocalProfileStartupTest [committed]; CachingConfigurationTest [untracked]
controller/: BookingControllerValidationTest [committed]
database/: ProductionSchemaRoutingTest [committed]; ProductionDatabaseConfigurationTest [modified]
dto/: PublicIdentifierContractTest [committed]
entity/: PublicUuidGenerationTest [committed]
security/: JwtAuthenticationFilterTest [committed]
service/: BookingServiceTest, DummyPaymentProcessorTest, PasswordResetServiceTest [committed]; AuthServiceLoginProtectionTest,
      PaymentServiceTest [modified]; BookingConfirmedEmailListenerTest, SignupOtpConfigurationServiceTest,
      SignupVerificationServiceTest [untracked]
```
`src/main/resources/`:
```
application.yml [modified], application-local.yml [modified], application-prod.yml [committed]
api-success-messages.json [modified], exception-messages.json [modified], security/open_apis.json [modified]
META-INF/spring.factories [committed]
.env/local.example.json [modified], .env/prod.example.json [modified], .env/local.json [gitignored], .env/prod.json [gitignored]
database/phase8-room-optimistic-lock.sql [committed], database/phase9-payment-transaction-constraint.sql [committed]
db/migration/V1..V4 [committed]; V5__create_signup_verifications.sql, V6__create_signup_otp_configuration.sql [untracked]
```
`src/test/resources/`: `application.properties` [committed], `.env/local.json` [committed; test fixture], `application-test.yml` [untracked].

### 5. DATABASE AND MIGRATIONS

Flyway (`src/main/resources/db/migration`); every file targets the quoted `"Production"` schema:
- **V1__initial_production_schema**: creates the schema plus `users`, `hotels`, `rooms` (`version`, unique `(hotel_id, room_number)`), `bookings` (`booking_uuid`, date-range CHECK), `payments` (`payment_uuid`, unique `booking_id`), indexes, and a partial unique index on `payments(transaction_id)` WHERE `status='PAID'`.
- **V2__copy_existing_public_data_to_production**: a `DO` block that copies `public.*` rows into `"Production"` if all five public tables exist; otherwise it skips. Copies only.
- **V3__add_failed_login_protection**: adds `users.failed_login_attempts` (backfilled to 0, then NOT NULL DEFAULT 0) and `users.locked_until`.
- **V4__create_password_reset_tokens**: creates `password_reset_tokens` (FK to users ON DELETE CASCADE, unique `token_hash`) with 2 indexes.
- **V5__create_signup_verifications** [untracked]: creates `signup_verifications` (unique email, password/otp hashes, `expires_at`, `attempt_count`, `resend_available_at`).
- **V6__create_signup_otp_configuration** [untracked]: creates a single-row `signup_otp_configuration` (CHECK id=1, CHECK `validity_minutes` 1–1440) and seeds `(1, 5)` ON CONFLICT DO NOTHING.

SQL outside Flyway, in `src/main/resources/database/` (manual, not run by anything):
- `phase8-room-optimistic-lock.sql`: `ALTER TABLE rooms ADD COLUMN version`. Not schema-qualified, so it targets the search_path (`public`). Superseded by V1.
- `phase9-payment-transaction-constraint.sql`: the same partial unique index as V1, on unqualified `payments` ("run once in Supabase SQL editor"). Superseded by V1.

JPA entities (all IDs use `@GeneratedValue(IDENTITY)` unless noted):
- `User` → `users`: id Long, name String, age Integer, email String, password String, role UserRole, createdAt LocalDateTime, failedLoginAttempts Integer, lockedUntil LocalDateTime, bookings List<Booking>
- `Hotel` → `hotels`: id Long, name String, city String, address String, description String, rooms List<Room>
- `Room` → `rooms`: id Long, **version long (@Version)**, roomNumber String, hotel Hotel, category RoomCategory, capacity Integer, price BigDecimal, bookings List<Booking>
- `Booking` → `bookings`: id Long, bookingUuid UUID, user User, room Room, startDate LocalDate, endDate LocalDate, status BookingStatus, createdAt LocalDateTime, payment Payment
- `Payment` → `payments`: id Long, paymentUuid UUID, booking Booking (OneToOne), amount BigDecimal, paidAmount BigDecimal, remainingAmount BigDecimal, method PaymentMethod, status PaymentStatus, transactionId String, createdAt LocalDateTime
- `PasswordResetToken` → `password_reset_tokens`: id Long, user User (LAZY), tokenHash String, expiresAt LocalDateTime, usedAt LocalDateTime, createdAt LocalDateTime
- `SignupVerification` → `signup_verifications`: id Long, email String, name String, age Integer, passwordHash String, otpHash String, expiresAt LocalDateTime, attemptCount Integer, resendAvailableAt LocalDateTime, createdAt LocalDateTime
- `SignupOtpConfiguration` → `signup_otp_configuration`: id Short (assigned, no generator), validityMinutes Integer, updatedAt LocalDateTime

### 6. CONFIGURATION

`application.yml` (working tree; no literal secrets):
```yaml
spring:
  application:
    name: hotel-booking
  profiles:
    default: local
  datasource:
    driver-class-name: org.postgresql.Driver
  data:
    redis:
      # Upstash TLS URL: rediss://default:<password>@<host>.upstash.io:6379
      # Blank means no Redis: an in-memory cache is used instead.
      # Read by config/CachingConfiguration, which also sets the 2s timeouts.
      url: ${REDIS_URL:}
  autoconfigure:
    # Boot's Redis auto-configuration fails startup on a blank URL, so
    # CachingConfiguration builds the connection only when REDIS_URL is set.
    exclude:
      - org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration
      - org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration
      - org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration
  jpa:
    properties:
      hibernate:
        format_sql: true

app:
  cors:
    # Local/test fallback. The prod profile overrides this with a required value.
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173}
  jwt:
    secret: ${JWT_SECRET}
    expiration: ${JWT_EXPIRATION:86400000}
  password-reset:
    delivery: ${PASSWORD_RESET_DELIVERY:console}
    frontend-base-url: ${FRONTEND_BASE_URL:http://localhost:5173}
    resend-api-key: ${RESEND_API_KEY:}
    mail-from: ${MAIL_FROM:}

management:
  health:
    redis:
      # Redis is an optional cache. Its outage must not mark the whole
      # application DOWN (and fail the Railway health check).
      enabled: false
```
`application-local.yml`:
```yaml
spring:
  config:
    activate:
      on-profile: local
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      # The Supabase session pool is small; local testing does not need the
      # default ten connections while Railway is also online.
      maximum-pool-size: 3
  jpa:
    hibernate:
      # Local now intentionally uses the deployed schema.  Hibernate must not
      # alter it from a developer machine.
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        # Preserve PostgreSQL's case-sensitive schema name exactly.
        default_schema: '"Production"'
  flyway:
    # Production migrations run in the Railway prod profile.  This local
    # profile only uses the already-migrated Production schema.
    enabled: false

app:
  cors:
    # Comma-separated origins can be supplied locally when needed.
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173}
```
`application-prod.yml`:
```yaml
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  flyway:
    enabled: true
    locations: classpath:db/migration
    default-schema: Production
    schemas:
      - Production
    create-schemas: true
    # "Production" may already contain tables from a prior safe deployment.
    # Record version 0 first, then allow V1/V2 to run their idempotent DDL/data-copy work.
    # Nothing is dropped, truncated, or overwritten.
    baseline-on-migrate: true
    baseline-version: "0"
    validate-migration-naming: true
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
    properties:
      hibernate:
        # Quotes preserve the exact case-sensitive PostgreSQL schema name.
        default_schema: '"Production"'

server:
  # Railway injects PORT at runtime. The fallback keeps direct local prod-profile
  # checks possible without changing the normal local profile.
  port: ${PORT:8080}
  address: ${SERVER_ADDRESS:0.0.0.0}

management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: never

app:
  password-reset:
    # Production sends only when Railway sets PASSWORD_RESET_DELIVERY=resend.
    delivery: ${PASSWORD_RESET_DELIVERY:noop}
  cors:
    # Required on Railway; specify one or more comma-separated HTTPS frontend origins.
    allowed-origins: ${CORS_ALLOWED_ORIGINS}
```
Env JSON files (key names only):
- `src/main/resources/.env/local.json`: **exists**. Keys: `DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, JWT_EXPIRATION`. No `REDIS_URL` (unset).
- `src/main/resources/.env/prod.json`: **exists**. Keys: `DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, JWT_EXPIRATION`. No `REDIS_URL` (unset).
- No `REDIS_URL`, `DB_*` or `JWT_*` variables are set in the shell environment.

Literal secrets committed in test resources (test-only dummies, but still literals): `src/test/resources/application.properties` has `app.jwt.secret=<REDACTED>`, and `src/test/resources/.env/local.json` has `JWT_SECRET=<REDACTED>`. Both decode to obvious dummy strings and match neither real file. The other values in those files are H2/fixture values.

`JsonEnvironmentPostProcessor` (registered in `META-INF/spring.factories`): `JSON_PROFILES = Set.of("local")`. It loads **only** `classpath:.env/local.json`, and only when the `local` profile is active (or is the default). **`prod.json` is never loaded by anything.** The JSON source ranks just below OS environment variables.

Locally (default profile `local`): the app connects to whatever `DB_URL` in `local.json` names. On this machine that is a **PostgreSQL on localhost:5432** (not reachable at the time of writing), and the schema is `"Production"`. **Flyway does not run locally.** `ddl-auto` is `none` locally and `none` in prod; tests use `create-drop` on H2.

### 7. API SURFACE

Auth is enforced by `JwtAuthenticationFilter` for paths under `/api/`; anything matching `open_apis.json` (method + path, `/**` wildcard) is public. `/actuator/**` is outside the filter.

| Method | Path | Controller method | Access |
|---|---|---|---|
| POST | /api/auth/register | AuthController.register | public |
| POST | /api/auth/verify-signup | AuthController.verifySignup | public |
| POST | /api/auth/resend-signup-otp | AuthController.resendSignupOtp | public |
| POST | /api/auth/login | AuthController.login | public |
| POST | /api/auth/forgot-password | AuthController.forgotPassword | public |
| POST | /api/auth/reset-password | AuthController.resetPassword | public |
| GET | /api/hotels | HotelController.getAllHotels | public |
| GET | /api/hotels/{hotelId} | HotelController.getHotel | public |
| GET | /api/hotels/search | HotelController.searchHotels | public |
| GET | /api/hotels/{hotelId}/rooms | HotelController.getHotelRooms | public |
| GET | /api/hotels/{hotelId}/rooms/available | HotelController.getAvailableRooms | public |
| GET | /api/rooms/{roomId} | RoomController.getRoom | public |
| POST | /api/bookings | BookingController.createBooking | authenticated |
| GET | /api/bookings/my | BookingController.getMyBookings | authenticated |
| GET | /api/bookings/{bookingId} | BookingController.getBooking | authenticated |
| PATCH | /api/bookings/{bookingId}/cancel | BookingController.cancelBooking | authenticated |
| POST | /api/payments/initiate/{bookingId} | PaymentController.initiatePayment | authenticated |
| POST | /api/payments/{paymentId}/submit | PaymentController.submitPayment | authenticated |
| GET | /api/payments/booking/{bookingId} | PaymentController.getPaymentForBooking | authenticated |
| GET | /api/user/me | UserController.currentUser | authenticated |

`open_apis.json`:
```json
[
  {"method": "POST", "path": "/api/auth/register"},
  {"method": "POST", "path": "/api/auth/verify-signup"},
  {"method": "POST", "path": "/api/auth/resend-signup-otp"},
  {"method": "POST", "path": "/api/auth/login"},
  {"method": "POST", "path": "/api/auth/forgot-password"},
  {"method": "POST", "path": "/api/auth/reset-password"},
  {"method": "GET", "path": "/api/hotels"},
  {"method": "GET", "path": "/api/hotels/**"},
  {"method": "GET", "path": "/api/rooms"},
  {"method": "GET", "path": "/api/rooms/**"}
]
```
(Reformatted onto one line per entry; the content is identical.)

### 8. SERVICES

The "Delivery mode" in this section is the value of `app.password-reset.delivery`.

- `ApiSuccessMessageCatalog`: `String get(String key, Object... arguments)`. Resolves success-message keys from JSON.
- `AuthService`: `AuthResponse register(RegisterRequest)` (**no callers since `/register` switched to OTP**); `AuthResponse login(LoginRequest)` (JWT login; 5 failures lock the account for 1 minute).
- `BookingService`: `BookingResponse createBooking(CreateBookingRequest)`; `List<BookingResponse> getMyBookings()`; `BookingResponse getMyBooking(UUID)`; `BookingResponse cancelBooking(UUID)`. Owner-scoped; checks overlap via count query plus an optimistic force-increment on the room.
- `DateRangeValidator`: package-private `static void validate(LocalDate, LocalDate)`. Check-in/check-out validation.
- `DummyPaymentProcessor` (implements `PaymentProcessor`): `boolean isSuccessful(String transactionId)`. Format-based fake gateway.
- `HotelService`: `List<HotelResponse> getAllHotels()`; `HotelResponse getHotelById(Long)`; `List<HotelResponse> searchHotels(String city, String name)`.
- `RoomService`: `List<RoomResponse> getRoomsByHotel(Long, Integer, RoomCategory, BigDecimal, BigDecimal)`; `RoomResponse getRoomById(Long)`; `List<RoomResponse> getAvailableRooms(Long, LocalDate, LocalDate, Integer, RoomCategory, BigDecimal, BigDecimal)`.
- `PaymentService`: `PaymentResponse initiatePayment(UUID)`; `PaymentSubmissionResult submitPayment(UUID, String)` (on success confirms the booking and publishes `BookingConfirmedEvent`); `PaymentResponse getPaymentForBooking(UUID)`.
- `PasswordResetService`: `void requestReset(ForgotPasswordRequest)`; `void resetPassword(ResetPasswordRequest)`. Hashed single-use tokens.
- `PasswordResetTokenDelivery` (interface): `void deliver(User, String rawToken)`. Implementations:
  - `DevelopmentPasswordResetTokenDelivery`: local profile, mode `console`; logs the token.
  - `NoOpPasswordResetTokenDelivery`: non-local, mode `noop`; does nothing.
  - `ResendPasswordResetTokenDelivery`: mode `resend`; sends an email.
- `SignupOtpDelivery` (interface): `void deliver(String email, String name, String otp, int validityMinutes)`. Implementations:
  - `DevelopmentSignupOtpDelivery`: local profile, mode `console`; logs the OTP.
  - `NoOpSignupOtpDelivery`: non-local, mode `noop`; **silently drops the OTP**.
  - `ResendSignupOtpDelivery`: mode `resend`; sends an email.
- `SignupVerificationService`: `void start(RegisterRequest)`; `AuthResponse verify(VerifySignupRequest)`; `void resend(ResendSignupOtpRequest)`. OTP signup flow.
- `SignupOtpConfigurationService`: `Duration getOtpValidity()` (@Cacheable; DB value or 300 s default); `void evictOtpValidity()` (@CacheEvict).
- `BookingConfirmationEmailService` (interface): `void sendBookingConfirmationEmail(BookingConfirmationEmailData)`. Implementation `ResendBookingConfirmationEmailService` (mode `resend`).
- `BookingConfirmedEmailListener` (mode `resend`): `void onBookingConfirmed(BookingConfirmedEvent)`. @TransactionalEventListener after commit; swallows send failures.
- `ResendEmailSender` (mode `resend`): package-private `void send(String to, String subject, String html, String text)`. Resend HTTP API call.

### 9. TESTS

- `HotelBookingApplicationTests`: contextLoads
- `config/CachingConfigurationTest`: blankRedisUrlUsesInMemoryCache, cacheFailuresDoNotPropagateAndFallBackToDatabase, unreachableRedisDoesNotPropagateAndFallsBackToDatabase, appSettingsSerializerRoundTripsDuration
- `config/JsonEnvironmentPostProcessorTest`: loadsJsonForActiveProfile, operatingSystemEnvironmentHasHigherPrecedenceThanJson, ignoresProfilesWithoutJsonConfiguration, doesNotLoadJsonForProductionProfile
- `config/LocalProfileStartupTest`: localProfileStartsWithJsonEnvironmentConfiguration
- `controller/BookingControllerValidationTest`: malformedUuidReturnsCleanBadRequest
- `database/ProductionDatabaseConfigurationTest`: productionDisablesHibernateDdlAndUsesQuotedSchema, localUsesProductionSchemaWithoutHibernateDdlOrMigrations, initialMigrationIsNonDestructiveAndSchemaQualified, existingPublicDataMigrationOnlyCopiesData, failedLoginProtectionMigrationIsNonDestructiveAndTargetsProductionUsers
- `database/ProductionSchemaRoutingTest`: hibernateRoutesEntityTablesToExactProductionSchema
- `dto/PublicIdentifierContractTest`: bookingResponseUsesUuidAsItsPublicId, paymentResponseUsesUuidsAsItsPublicIds
- `entity/PublicUuidGenerationTest`: bookingUuidsAreGeneratedAndUnique, paymentUuidsAreGeneratedAndUnique
- `security/JwtAuthenticationFilterTest`: protectedApiWithoutJwtReturnsStandardUnauthorizedResponse
- `service/AuthServiceLoginProtectionTest`: correctPasswordReturnsJwtAndResetsLoginProtection, oneWrongPasswordPersistsOneFailedAttempt, fourthWrongPasswordIsStillGenericUnauthorized, fifthWrongPasswordLocksAccountAndPersistsState, correctPasswordWhileLockedDoesNotVerifyOrExtendLock, wrongPasswordWhileLockedDoesNotExtendLock, expiredLockWithCorrectPasswordSucceedsAndClearsState, expiredLockWithWrongPasswordStartsAgainAtOne, successfulLoginAfterThreeFailuresResetsCounter, unknownEmailUsesTheSameGenericInvalidCredentialsError, registrationInitializesLoginProtectionFields
- `service/BookingConfirmedEmailListenerTest`: sendsConfirmationToBookingOwner, deliveryFailureDoesNotEscapeAfterCommitListener
- `service/BookingServiceTest`: bookingCreationLoadsRoomWithOptimisticForceIncrement, newlyConstructedBookingsReceiveUniquePublicReferences, retrievesOwnedBookingByPublicReference, foreignBookingReferenceIsReportedAsNotFound, cancelsOwnedBookingByPublicReference, overlappingBookingIsRejectedBeforeInsert
- `service/DummyPaymentProcessorTest` (parameterized): acceptsExactDummyTransactionFormat, rejectsAnythingOutsideExactDummyTransactionFormat
- `service/PasswordResetServiceTest`: requestForExistingUserStoresHashAndDeliversOnlyRawToken, unknownEmailCreatesNoTokenAndHasNoDelivery, hashingIsDeterministicAndDoesNotStoreRawToken, validResetChangesPasswordMarksTokenUsedAndClearsLock, usedOrExpiredTokenIsRejected
- `service/PaymentServiceTest`: initiationCalculatesAmountFromNightlyRoomPrice, validTransactionPaysAndConfirmsBooking, invalidTransactionFailsAndCancelsBookingWithoutThrowing, duplicateSuccessfulTransactionDoesNotChangeSecondPaymentOrBooking, foreignBookingIsReportedAsNotFound, foreignPaymentIsReportedAsNotFound, retrievesPaymentForOwnedBookingReference
- `service/SignupOtpConfigurationServiceTest`: returnsDatabaseValueWhenRowIsValid, returnsDefaultWhenRowIsMissing, returnsDefaultWhenValueIsNull, returnsDefaultWhenValueIsOutOfRange (parameterized), returnsDefaultWhenTableCannotBeRead
- `service/SignupVerificationServiceTest`: startStoresOnlyHashesAndDoesNotCreateUser, correctOtpCreatesUserAndDeletesPending, wrongOtpPersistsAttempt

### 10. REDIS / CACHING — CURRENT STATE

- **Files:** added `config/CachingConfiguration.java`, `CachingConfigurationTest`, `SignupOtpConfigurationServiceTest`. Changed `pom.xml`, `application.yml`, both `*.example.json`, `SignupOtpConfigurationService` (rewritten), a one-line change in `SignupVerificationService` and its test. No new table or migration: the existing V6 `signup_otp_configuration` is the source of truth. This was a deliberate decision (option A) instead of a generic `app_settings` table.
- **Dependency:** yes, `spring-boot-starter-data-redis` with no version (managed by Boot 4.1.1).
- **Configuration:**
  - Cache name `appSettings`, TTL 60 s, null values not cached.
  - Key prefix `hotelbooking:`, so the full key is `hotelbooking:appSettings::otp.validity.seconds`.
  - Keys use `StringRedisSerializer`. `appSettings` values use `JacksonJsonRedisSerializer<Duration>` (Jackson 3). The default for other caches is `GenericJacksonJsonRedisSerializer` with a restricted type validator.
  - The typed serializer was required because the generic serializer returns a `Duration` as the String "PT5M", which would have caused a ClassCastException on the first cache hit.
  - Lettuce: connect and command timeouts of 2 s, `REJECT_COMMANDS` while disconnected; TLS and credentials are taken from the URL.
- **Annotations:** `@Cacheable` on `SignupOtpConfigurationService.getOtpValidity()`; `@CacheEvict` on `evictOtpValidity()`. **Nothing calls evict.**
- **CacheErrorHandler:** yes (`WarningCacheErrorHandler`). It logs get/put/evict/clear failures at WARN and swallows them, so the method body (the DB read) runs.
- **Blank `REDIS_URL`:** an in-memory `ConcurrentMapCacheManager` (no TTL). Boot's Redis auto-configuration is excluded because Boot 4 refuses to start on a blank URL; the Lettuce factory is built only when the URL is set.
- **Actuator Redis health indicator:** disabled.
- **Test coverage:**
  - `cacheFailuresDoNotPropagateAndFallBackToDatabase`: a throwing cache, and the DB is hit on every call. This proves the degradation path through the real Spring cache proxy.
  - `unreachableRedisDoesNotPropagateAndFallsBackToDatabase`: a real Lettuce client against 127.0.0.1:1. This proves it end-to-end without a real Redis.
  - `blankRedisUrlUsesInMemoryCache`: the in-memory backend, caching and eviction.
  - Serializer round-trip test, plus 8 service tests for the fallback logic.
- **Verification:** **never run against a real Upstash instance.** It was manually run in the previous task with a blank URL and with a wrong `*.upstash.io` host (H2 database, throwaway JWT secret). All requests returned 200, and the wrong-host run logged "Cache get failed" warnings. With an unreachable host, each signup added ~1–2 s.

### 11. OPEN QUESTIONS — answer these directly

1. **True:** `default_schema "Production"`, Flyway disabled, `ddl-auto: none`. **On this machine, local does not hit the deployed database**: `local.json`'s `DB_URL` points at localhost:5432. The yml comments ("Supabase session pool", "uses the deployed schema") show the *intended* setup is to point `local.json` at the deployed Supabase DB. In that setup, local dev reads and writes the same tables as production, and a local test signup would insert a `signup_verifications` row, and on verify a `users` row, visible in production. Note that V5/V6 have never been deployed and local Flyway is off, so in that setup a local signup would currently fail with a missing table.
2. `.env/prod.json` **exists** on disk. It is **not loaded** by anything: `JsonEnvironmentPostProcessor` only loads `local`. It is **gitignored** (`src/main/resources/.env/*.json`). It has **never been committed**: `git log --all` for the path is empty, and the same is true for `local.json`. **However, it is packaged into any locally built jar** (`BOOT-INF/classes/.env/prod.json`, verified in `target/hotel-booking-0.0.1-SNAPSHOT.jar`).
3. **No** in the hotel app: nothing reads a user's role to reject a request. `UserRole.ADMIN` exists and the role is put into the JWT and `/api/user/me`, but nothing enforces it. **Yes** in the sibling `transport-service`: `AdminAuthorizationService` rejects non-ADMIN users with 403 for `/api/admin/**`, using the role returned by the hotel app's `/api/user/me`.
4. **No.** No EXCLUDE/gist constraint in any migration (hotel or transport). Overlaps are prevented in application code: a count query plus `OPTIMISTIC_FORCE_INCREMENT` on the room's `@Version`, backed by the index `idx_bookings_room_dates_status`.
5. **Yes.** `transport-service/` (untracked; trips, seats, vehicles, drivers, routes, transport bookings and payments, admin endpoints; its own `transport` schema, Flyway V1–V5) and the frontend's `transportApi.js` and `Transport*Page.jsx`. None of it is in git.

### 12. YOUR ASSESSMENT

**Would break if deployed right now (assuming everything were committed):**
- **Signup in prod.** `/register` no longer creates users. In prod with the default `PASSWORD_RESET_DELIVERY=noop`, `NoOpSignupOtpDelivery` throws the OTP away, so no new user can ever sign up. It only works if Railway sets `resend`, plus the Resend key and sender.
- **Partial commits.** The OTP code depends on the untracked V5/V6, DTOs, entities, the event package and `application-test.yml`. Committing only the "modified" files would break compilation or tests.
- **The frontend's transport pages call `localhost:8081`** unless `VITE_TRANSPORT_API_BASE_URL` is set, and `transport-service` has no deployment config in this repo.
- **The pushed HEAD's own test suite most likely fails:** the committed `AuthService` locks for 1 minute, while the committed `AuthServiceLoginProtectionTest` expects ~15 minutes. Inferred from reading; not run at HEAD.

**Wrong, risky or inconsistent:**
- **Locally built jars contain the real `local.json` and `prod.json`.** There is no resource exclusion in the pom. A stale jar with them exists at `target/hotel-booking-0.0.1-SNAPSHOT.jar`; it was built by the assistant during the Redis task. `prod.json` serves no purpose at runtime.
- **The local secrets file is broken on this machine:** its `JWT_SECRET` is not valid base64 (startup fails), and `DB_URL` targets a localhost Postgres that isn't running. So local startup fails today regardless of code.
- **The login lock is only 1 minute** (`LOCK_DURATION_MINUTES = 1`), which looks like a debugging value.
- **One property controls three things:** `app.password-reset.delivery` governs signup OTP and booking emails as well. The `local` profile combined with `noop` leaves no `SignupOtpDelivery` bean, so startup fails.
- **Dead code:** `AuthService.register` still exists and creates unverified users if anyone wires it back.
- **Unmanaged SQL:** `database/phase8`/`phase9` are manual, unqualified scripts that duplicate V1; they target `public`.
- **Inconsistent style:** the new OTP classes are written in a compressed one-line style, unlike the rest of the codebase.

**From the earlier Redis task; not fully confident:**
- Not verified against real Upstash.
- The in-memory fallback has no TTL, so direct DB edits require a restart locally.
- The OTP read uses a `REQUIRES_NEW` transaction, which takes a second pool connection; the local pool max is 3.
- With an unreachable Redis there is no circuit breaker, so each cached call pays a connection attempt (~1–2 s observed).
- The Redis health indicator is disabled, so a Redis outage is visible only in WARN logs.
- `local.example.json` contains a fake (non-blank) `REDIS_URL`. A teammate who copies it unedited gets the noisy degraded path.

**Surprises:**
- A whole second microservice (`transport-service`, ~90 files) plus frontend pages exists only as untracked files.
- `/register` changed semantics (from 201 + user to 200 + OTP) without the old method being removed.
- The secrets files are bundled into the jar.
- Committed test resources contain (dummy) JWT secrets.

*Report produced read-only. Test output was written to a temp file outside the repo; the only file created in the repo is this report.*
