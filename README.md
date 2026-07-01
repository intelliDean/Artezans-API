# Artezans — Task Hub API: Comprehensive Technical Documentation

> **Internal project identity:** The repository is named **Artizans** but the application calls itself **Task Hub** in all runtime identifiers (Swagger title, admin email, default app name, etc.). This document uses **Task Hub / Artezans** interchangeably.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Architecture](#3-architecture)
4. [Domain Model](#4-domain-model)
5. [Modules & Packages](#5-modules--packages)
6. [API Endpoints Reference](#6-api-endpoints-reference)
7. [Security Model](#7-security-model)
8. [Payment Integrations](#8-payment-integrations)
9. [Notification System](#9-notification-system)
10. [Database Schema](#10-database-schema)
11. [Configuration Reference](#11-configuration-reference)
12. [Seeded Data & Initialization](#12-seeded-data--initialization)
13. [Running the Application](#13-running-the-application)
14. [Spring Boot 4 Migration Notes](#14-spring-boot-4-migration-notes)
15. [Known Constraints & Future Work](#15-known-constraints--future-work)

---

## ⚡ Quick Start

### Backend (Spring Boot)
```bash
# Copy and fill in required env vars
cp .env.example .env

# Run with Maven wrapper (Flyway migrations apply automatically)
./mvnw spring-boot:run

# API available at:  http://localhost:8080
# Swagger UI at:     http://localhost:8080/swagger-ui/index.html
```

### Frontend (React + Vite)
```bash
cd frontend

# Install dependencies
npm install

# Copy and fill in frontend env vars
cp .env.example .env

# Start the dev server (auto-proxies /api to localhost:8080)
npm run dev

# App available at: http://localhost:5173
```

---

## ✅ Implemented Features (Steps 1–9)

| Step | Feature | Status |
|------|---------|--------|
| 1 | Base setup, design system, JWT auth flow | ✅ Done |
| 2 | Homepage, live task feed (TanStack Query polling) | ✅ Done |
| 3 | Task posting modal, Customer Dashboard, bids drawer | ✅ Done |
| 4 | Bookings lifecycle, Stripe checkout, PayPal sandbox | ✅ Done |
| 5 | Provider Workspace (tasks, bookings, listings CRUD) | ✅ Done |
| 6 | Service listings directory with search & direct booking | ✅ Done |
| 7 | Admin Dashboard (user management, category tools) | ✅ Done |
| 8 | Task bids feed, provider bid submission, bid sync | ✅ Done |
| 9 | Customer reviews & ratings — database-backed with REST API | ✅ Done |

---

## 📋 Review API Reference

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/v1/review/submit` | `CUSTOMER` | Submit a star rating and comment for a completed booking |
| `GET` | `/api/v1/review/provider?email={email}` | Public | Fetch all reviews for a given provider email |
| `GET` | `/api/v1/review/booking/{bookingId}` | Public | Retrieve the review submitted for a specific booking |

### Review Request Body
```json
{
  "bookingId": 42,
  "providerEmail": "chiamaka@gmail.com",
  "rating": 5,
  "comment": "Exceptional work, very professional!"
}
```

---

## 1. Project Overview

Artezans is a **RESTful artezanService-marketplace backend** that connects customers who need work done with artezanService providers who can perform that work. Think of it as a local-artezanServices platform (similar conceptually to Airtasker or TaskRabbit) tailored to the Australian market.

### Core Value Proposition

| Actor | Capability |
|-------|-----------|
| **Customer** | Register, browse listings, post tasks, book providers, pay securely |
| **Service Provider** | Register, submit KYC identity, create artezanService listings, accept/reject proposals, earn payments |
| **Admin** | Manage categories/artezanServices, oversee listings and tasks, run scripts |

### Key Features

- **Two-sided marketplace** — distinct Customer and ServiceProvider personas built on a common `User` base entity.
- **Full booking lifecycle** — `PROPOSED → PENDING → ACCEPTED/REJECTED → PAID` with both provider and customer acceptance stages.
- **Dual payment rails** — Stripe Payment Intents and PayPal REST SDK, customer's choice.
- **Email & SMS notifications** — Brevo (formerly Sendinblue) transactional API for both channels.
- **Push-style app notifications** — persisted `AppNotification` records per user.
- **Media uploads** — Cloudinary integration for profile pictures and listing business photos.
- **Full-text search** — PostgreSQL `tsvector` columns with GIN indices and `pg_trgm` extension on users, listings, tasks, and addresses.
- **JWT authentication** — short-lived access tokens (default 3 h) + long-lived refresh tokens (default 168 h / 7 days).
- **OAuth2 scaffold** — client-side OAuth2 infrastructure wired but currently commented-out pending activation.
- **OpenAPI / Swagger UI** — live documentation at `/swagger-ui.html`.

---

## 2. Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 21 (LTS) |
| Framework | Spring Boot | **4.0.6** |
| Web | Spring MVC (+ WebFlux on classpath) | 7.x |
| Security | Spring Security | 7.x |
| Persistence | Spring Data JPA + Hibernate | 7.x |
| Database | PostgreSQL | any 14+ |
| Migrations | Flyway | 11.x (via `spring-boot-starter-flyway`) |
| Build | Apache Maven | 3.9.x (wrapper included) |
| JSON | Jackson (2.x legacy bridge + `jjwt-jackson`) | — |
| JWT | JJWT | 0.12.6 |
| Mapping | ModelMapper | 3.2.1 |
| JSON Patch | java-json-tools/json-patch | 1.13 (RFC 6902) |
| Templates | Thymeleaf + thymeleaf-extras-springsecurity6 | — |
| API Docs | springdoc-openapi-starter-webmvc-ui | 2.8.8 |
| Payments | Stripe Java SDK | 26.6.0 |
| Payments | PayPal REST SDK | 1.14.0 |
| Email/SMS | Brevo (sib-api-v3-sdk) | 7.0.0 |
| Media | Cloudinary (cloudinary-http45) | 1.37.0 |
| Env vars | java-dotenv | 5.2.2 |
| Lombok | Project Lombok | 1.18.46 |

---

## 3. Architecture

### 3.1 Layered Architecture Pattern

The codebase follows a **Gateway → Controller → Service → Repository** layered pattern. This is a deliberate separation that avoids the common anti-pattern of mixing HTTP concerns with business logic.

```
┌─────────────────────────────────────────────────────┐
│  HTTP Layer  (gateway.*)                             │
│  @RestController  — handles HTTP verb, path,        │
│  request/response types, Swagger annotations,       │
│  Spring Security @PreAuthorize                      │
└──────────────────────┬──────────────────────────────┘
                       │ delegates to
┌──────────────────────▼──────────────────────────────┐
│  Controller Layer  (*.controller.*)                  │
│  @Component  — orchestrates calls between artezanServices  │
│  No HTTP knowledge; clean unit-testable             │
└──────────────────────┬──────────────────────────────┘
                       │ calls
┌──────────────────────▼──────────────────────────────┐
│  Service Layer  (*.artezanService.* / *.artezanServices.*)         │
│  @Service / @Profile-guarded impls                  │
│  Business logic, transactions, validation           │
└──────────────────────┬──────────────────────────────┘
                       │ queries
┌──────────────────────▼──────────────────────────────┐
│  Repository Layer  (Spring Data JPA repositories)   │
│  JPA + Flyway-managed PostgreSQL                    │
└─────────────────────────────────────────────────────┘
```

### 3.2 Package Map

```
com.api.artezans
├── Artezans.java              ← @SpringBootApplication entry point
├── CorsConfig.java            ← CORS (currently wide-open: all origins/methods)
├── InitActors.java            ← @PostConstruct seed: admin, demo customer, demo provider
├── OpenApiConfig.java         ← Swagger/OpenAPI bean configuration
│
├── admin/                     ← AdminService (admin-specific login logic)
├── authentication/            ← AuthService + DTOs (login/logout/refresh)
├── booking/                   ← Full booking lifecycle
├── category/                  ← Service category management
├── config/                    ← Cross-cutting: Security, JWT filter, OAuth2, utils
├── customer/                  ← Customer persona management
├── exceptions/                ← Global exception handler + exception types
├── gateway/                   ← HTTP entry points (all @RestController beans)
│   ├── admin/                 ←   POST api/v1/admin/**
│   ├── authentication/        ←   POST api/v1/auth/**
│   ├── booking/               ←   POST api/v1/booking/**
│   ├── customer/              ←   POST/PATCH api/v1/customer/**
│   ├── listing/               ←   GET/POST/PATCH/DELETE api/v1/listing/**
│   ├── password/              ←   POST api/v1/password/**
│   ├── provider/              ←   GET/POST/PATCH api/v1/service_provider/**
│   ├── stripe/                ←   Stripe webhook entry
│   ├── task/                  ←   Task gateway
│   └── user/                  ←   POST api/v1/user/**
├── listings/                  ← Listing CRUD + search
├── multimedia/                ← Cloudinary upload abstraction
├── notifications/             ← app_notification + mail + sms
├── password/                  ← Forgot/reset password + change password
├── payment/                   ← Stripe + PayPal integration
├── provider/                  ← ServiceProvider persona + identity verification
├── task/                      ← Customer task posting
├── tokens/                    ← JWT token + verification token management
├── users/                     ← Base User entity + roles
└── utils/                     ← Shared utilities (ApiResponse, Paginate, etc.)
```

---

## 4. Domain Model

### 4.1 Core Entities

```
┌──────────┐ 1     1 ┌──────────────┐
│   User   │─────────│   Customer   │
│          │         └──────────────┘
│  - id                              
│  - firstName                       
│  - lastName                        
│  - emailAddress (unique, immutable)
│  - password                        
│  - phoneNumber                     
│  - address (→ Address)             
│  - profileImage (Cloudinary URL)   
│  - isEnabled                       
│  - accountState                    
│  - deactivatedAt                   
│  - registeredAt                    
│  - roles (Set<Role>)               
│  - stripeId                        
│  - appNotificationList             
└──────────┘ 1     1 ┌─────────────────┐
                     │ ServiceProvider  │
                     │ - userIdentity   │
                     │   (UserIdentity) │
                     └─────────────────┘
                           │ 1
                           │ *
                     ┌─────────────────┐
                     │     Listing      │
                     │ - businessName   │
                     │ - serviceCategory│
                     │ - serviceName    │
                     │ - serviceDescription│
                     │ - pricing        │
                     │ - availableDays  │
                     │ - availableFrom/To│
                     │ - address        │
                     │ - businessPictures│
                     │ - stripeId       │
                     │ - deleted        │
                     └─────────────────┘
                           │ 1
                           │ 1
                     ┌─────────────────┐
                     │     Booking      │
                     │ - bookDates      │
                     │ - bookFrom/To    │
                     │ - totalCost      │
                     │ - bookState      │
                     │ - bookingStage   │
                     │ - accepted       │
                     │ - bookingAgreement│
                     └─────────────────┘
```

### 4.2 Enumerations

| Enum | Values |
|------|--------|
| `Role` | `ADMIN`, `CUSTOMER`, `SERVICE_PROVIDER` |
| `AccountState` | `VERIFIED`, `UNVERIFIED`, `DEACTIVATED` |
| `BookingStage` | `PROPOSED`, `PENDING`, `REJECTED`, `ACCEPTED`, `PAID` |
| `BookingState` | (in-package enum, tracks payment state) |
| `AvailableDays` | Days of week for listing availability |
| `IdType` | Government ID types for KYC |

### 4.3 Booking Lifecycle State Machine

```
 [Customer books]
       │
       ▼
  PROPOSED ──────────────────────────────►  REJECTED (provider rejects)
       │
 [Provider accepts proposal]
       │
       ▼
  PENDING
       │
 [Customer pays — Stripe or PayPal]
       │
       ▼
  PAID ──► [Notification sent to provider]
       │
 [Provider completes work]
       │
       ▼
  ACCEPTED (customer accepts completed artezanService)
   OR
  customerRejectService() ─► REJECTED
```

---

## 5. Modules & Packages

### 5.1 `authentication` — Auth Service

Handles standard email/password authentication.

- **`AuthService`** — `authenticateAndGetToken(AuthRequest)`, `logout()`, `refreshToken()`
- **`JwtAuthFilter`** — `OncePerRequestFilter`; extracts Bearer token, validates, and populates `SecurityContextHolder`
- **`AuthProvider`** — `DaoAuthenticationProvider` bean

### 5.2 `config` — Cross-Cutting Configuration

| File | Purpose |
|------|---------|
| `SecurityConfig.java` | `SecurityFilterChain` bean, stateless JWT sessions, whitelist, method security |
| `JwtAuthFilter.java` | JWT validation filter (runs before `UsernamePasswordAuthenticationFilter`) |
| `CorsConfig.java` | CORS — currently `allowedOrigins("*")`, all methods |
| `config/utils/AppConfig.java` | Legacy `ObjectMapper` bean (Jackson 2.x bridge for Spring Boot 4) |
| `config/utils/NoAuth.java` | Centralised whitelist array for unauthenticated endpoints |
| `config/Oauth2/` | OAuth2 cookie repository + failure handler (success handler commented-out) |
| `config/annotation/@CurrentUser` | Custom annotation to resolve `SecuredUser` from `SecurityContext` |
| `config/security/SecuredUser` | `UserDetails` wrapper exposing the domain `User` |

### 5.3 `booking` — Booking Lifecycle

The most complex module. Orchestrates the multi-step deal flow:

1. `bookService()` — customer creates a booking proposal for a listing
2. `acceptProposal()` / `rejectProposal()` — provider responds
3. `createPaymentIntentWithStripe()` / `authorizePaymentWithPaypal()` — customer pays
4. `executePaymentWithPaypal()` — PayPal callback completes payment
5. `updateBookingAfterPayment()` — syncs booking state post-payment
6. `sendServiceProviderNotificationAfterPayment()` — triggers provider alert
7. `completeTask()` — provider marks work done
8. `customerAcceptService()` / `customerRejectService()` — customer reviews
9. `generateInvoice()` — generates final invoice

### 5.4 `listings` — Service Listings

Providers create rich listings representing their offered artezanServices. Supports:
- CRUD with JSON Patch (RFC 6902) updates
- Pagination
- Soft delete (`deleted` flag)
- Location-based filtering
- Full-text search by artezanService name
- Multiple business photos (Cloudinary URLs stored as `@ElementCollection`)

### 5.5 `provider` — Service Provider

Two-step registration:
1. **Sign-up** — basic details, triggers verification email
2. **Complete** — multipart form with `ServiceProviderUpdateRequest` (includes `UserIdentity` KYC documents)

After registration, providers can:
- Upload profile picture (Cloudinary)
- Update info via JSON Patch
- View tasks that match their artezanService skills (`serviceProviderViewPeculiarTasks`)
- View app notifications

### 5.6 `customer` — Customer

Two-step registration (mirroring provider):
1. **Sign-up** — basic details
2. **Complete** — `CustomerUpdateRequest` with additional profile data

Customers can update their profile via JSON Patch and upload a profile image.

### 5.7 `task` — Task Posting

Customers can post tasks describing work they need done. A task includes:
- `taskServiceName`, `taskDescription`
- `userAddress` (serialised location)
- `taskDates` (Set of dates)
- `customerBudget`
- `taskImage` (Cloudinary URL)
- `isActive` flag

Service providers see "peculiar tasks" matched to their listed artezanService categories.

### 5.8 `category` — Service Taxonomy

Two-level taxonomy:
- **Category Name** (e.g., "HOME SERVICES") — top-level bucket
- **Category** → **Services** — `Category` contains many `Service` records (e.g., "Cleaning", "Plumbing")

Admin-managed. 15 pre-seeded categories and 63 artezanServices (via `V1__db_setup.sql`).

### 5.9 `tokens` — Token Management

| Token | Purpose |
|-------|---------|
| `TaskHubToken` | Access + refresh JWT pair; persisted per user; revocable |
| `TaskHubVerificationToken` | Email verification during registration; auto-expired via `@Scheduled` |
| `ChangePasswordToken` | One-time password-reset token |

Scheduled cleanup jobs (`@Scheduled`) remove expired tokens (methods are package-private per Spring Framework 7 proxy requirement).

### 5.10 `exceptions` — Global Error Handling

`TaskHubExceptionHandler` (`@RestControllerAdvice`) converts:

| Exception | HTTP Status |
|-----------|------------|
| `TaskHubException` | 400 Bad Request (with custom status override) |
| `MethodArgumentNotValidException` | 400 Bad Request + field-level error map |
| `UserNotAuthorizedException` | 400 Bad Request |
| `UserNotFoundException` | 400 Bad Request |
| `BadCredentialsException` | 400 Bad Request |

> **Note:** Future improvement — `UserNotAuthorizedException` should return 403, `UserNotFoundException` should return 404.

### 5.11 `multimedia` — Media Uploads

Service interface `MultimediaService` with two implementations:

| Impl | Profile | Description |
|------|---------|-------------|
| `CloudinaryMultimediaServiceImpl` | `@Profile("!default")` | Production Cloudinary upload |
| `TestMultimediaImpl` | Default (no profile annotation) | Returns a dummy URL for local dev/testing |

### 5.12 `notifications`

Three sub-systems:

| Sub-system | Transport | Provider |
|-----------|-----------|---------|
| `app_notification` | In-app (persisted `AppNotification` entity) | Internal DB |
| `mail` | Email | Brevo transactional SMTP API |
| `sms` | SMS | Brevo transactional SMS API |

`BrevoMailImpl` is `@Profile("!default")` — a test stub handles the default profile.

---

## 6. API Endpoints Reference

All routes are prefixed with the server context root. The base API version prefix is **`/api/v1/`**.

### 6.1 Authentication — `api/v1/auth`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/auth/login` | Public | Authenticate user, returns `AuthResponse` (access + refresh tokens) |
| `POST` | `/api/v1/auth/logout` | Public | Invalidates current session tokens |
| `POST` | `/api/v1/auth/refresh` | Public | Issues new access token using refresh token from header |

### 6.2 Admin — `api/v1/admin`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/admin/login` | Public | Admin-specific login |
| `POST` | `/api/v1/admin/script` | Public | Run a SQL script (`script/test_script.sql`) |
| `GET` | `/api/v1/admin/listings/{pageNumber}` | `ADMIN` | Paginated list of ALL listings |
| `GET` | `/api/v1/admin/admin-listing/{listingId}` | Auth | Find any listing by ID (admin view) |
| `POST` | `/api/v1/admin/add-category-name` | `ADMIN` | Add new top-level category names |
| `GET` | `/api/v1/admin/all-category-names` | Auth | List all category names |
| `POST` | `/api/v1/admin/add-artezanService-category` | `ADMIN` | Add a artezanService to a category |
| `GET` | `/api/v1/admin/get-artezanService-name/{categoryName}` | Auth | List artezanServices under a category |
| `DELETE` | `/api/v1/admin/delete` | `ADMIN` | Delete a category name |
| `GET` | `/api/v1/admin/categories` | Auth | View all categories |
| `GET` | `/api/v1/admin/admin-view-tasks` | Auth | View all posted tasks |

### 6.3 Customer — `api/v1/customer`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/customer/sign-up` | Public | Register a new customer account |
| `POST` | `/api/v1/customer/complete` | Public | Complete registration (token + update payload) |
| `POST` | `/api/v1/customer/profile_picture` | `CUSTOMER` | Upload profile image to Cloudinary |
| `PATCH` | `/api/v1/customer/update` | `CUSTOMER` | Partial update via RFC 6902 JSON Patch |

### 6.4 Service Provider — `api/v1/service_provider`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/service_provider/sign-up` | Public | Register a new artezanService provider |
| `POST` | `/api/v1/service_provider/complete` | Public | Complete registration (multipart/form-data) |
| `POST` | `/api/v1/service_provider/profile_picture` | `SERVICE_PROVIDER` | Upload profile picture |
| `PATCH` | `/api/v1/service_provider/update` | `SERVICE_PROVIDER` | Update info via JSON Patch |
| `GET` | `/api/v1/service_provider/peculiar-tasks` | `SERVICE_PROVIDER` | View tasks matching the provider's skills |
| `GET` | `/api/v1/service_provider/notifications` | `SERVICE_PROVIDER` | View all app notifications |

### 6.5 Listing — `api/v1/listing`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/listing/create-listing` | Public* | Create a new artezanService listing (multipart) |
| `GET` | `/api/v1/listing/listing-images/{listingId}` | Auth | Get all business picture URLs |
| `GET` | `/api/v1/listing/listings/{pageNumber}` | `SERVICE_PROVIDER` | Paginated listings for authenticated provider |
| `GET` | `/api/v1/listing/undeleted/{pageNumber}` | Auth | Paginated non-deleted listings |
| `PATCH` | `/api/v1/listing/update-listing/{listingId}` | `SERVICE_PROVIDER` | JSON Patch update |
| `DELETE` | `/api/v1/listing/delete-listing/{listingId}` | `SERVICE_PROVIDER` | Soft delete listing |
| `GET` | `/api/v1/listing/by-id/{listingId}` | Auth | Get listing by ID (user view) |
| `GET` | `/api/v1/listing/by-artezanService-name/{serviceName}` | Auth | Search listings by artezanService name |
| `GET` | `/api/v1/listing/by-location` | Auth | Filter listings by location (suburb/state/postcode) |

> *`create-listing` has its `@PreAuthorize` commented out — currently open.

### 6.6 Booking — `api/v1/booking`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/booking/book` | `CUSTOMER` or `SERVICE_PROVIDER` | Create a booking proposal |
| `POST` | `/api/v1/booking/accept-proposal?bookingId=` | Auth | Provider accepts booking |
| `POST` | `/api/v1/booking/reject-proposal?bookingId=` | Auth | Provider rejects booking |
| `POST` | `/api/v1/booking/complete-task?bookingId=` | Auth | Provider marks task complete |
| `POST` | `/api/v1/booking/payment-intent-stripe/{bookingId}` | Auth | Create Stripe PaymentIntent |
| `POST` | `/api/v1/booking/authorize-paypal?bookingId=` | Auth | Initiate PayPal payment |
| `POST` | `/api/v1/booking/execute-payment-paypal?paymentId=&payerId=` | Public | PayPal callback — complete payment |
| `POST` | `/api/v1/booking/send-notification?bookingId=` | Auth | Notify provider after payment |
| `POST` | `/api/v1/booking/accept-artezanService?bookingId=` | Auth | Customer accepts completed artezanService |
| `POST` | `/api/v1/booking/reject-artezanService` | Auth | Customer rejects artezanService (with reason) |
| `POST` | `/api/v1/booking/update_payment?bookingId=` | Auth | Sync booking state after payment |
| `POST` | `/api/v1/booking/generate-invoice?bookingId=` | Auth | Generate invoice |
| `POST` | `/api/v1/booking/stripe-webhook` | Public | Stripe event webhook receiver |

### 6.7 User — `api/v1/user`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/user/forgot-password` | Public | Request password reset link |
| `POST` | `/api/v1/user/reset-password?t={token}` | Public | Reset password using token |
| `POST` | `/api/v1/user/verify?t={token}&e={email}` | Public | Verify email address |
| `POST` | `/api/v1/user/deactivate` | Auth | Deactivate own account |
| `POST` | `/api/v1/user/activation-mail` | Public | Re-send activation email |
| `POST` | `/api/v1/user/reactivate?e={email}&t={token}` | Public | Reactivate account |

### 6.8 Password Change — `api/v1/password`

Handled by `ChangePasswordGateway`:

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/password/change` | Auth | Initiate authenticated password change |

### 6.9 PayPal Direct — `api/v1/paypal`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/v1/paypal/success?paymentId=&payerId=` | Public | PayPal success redirect handler |
| `GET` | `/api/v1/paypal/cancel` | Public | PayPal cancelled payment handler |

---

## 7. Security Model

### 7.1 JWT Authentication Flow

```
Client                        Server
  │                              │
  │─── POST /api/v1/auth/login ──►│
  │    { email, password }        │  1. AuthenticationManager authenticates
  │                               │  2. Existing tokens revoked
  │◄── { accessToken, refreshToken }  3. New token pair persisted + returned
  │                              │
  │─── GET  /api/v1/... ─────────►│
  │    Authorization: Bearer <AT> │  JwtAuthFilter validates AT
  │                               │  → SecurityContextHolder populated
  │◄── 200 OK  ──────────────────│
  │                              │
  │─── POST /api/v1/auth/refresh ─►│  (when AT expires)
  │    Authorization: Bearer <RT> │  1. RT validated + not revoked
  │◄── new AT written to response │  2. Fresh AT issued
```

### 7.2 Token Storage

Tokens are **persisted** in the `task_hub_token` table (not just stateless). This enables:
- Server-side revocation on logout
- Refresh token rotation

### 7.3 Roles & Authorities

| Role | Access Level |
|------|-------------|
| `ADMIN` | Admin-only endpoints + all authenticated endpoints |
| `SERVICE_PROVIDER` | Provider-specific endpoints + shared booking flows |
| `CUSTOMER` | Customer-specific endpoints + shared booking flows |

Method-level security uses `@PreAuthorize("hasAuthority('ROLE_NAME')")`.

### 7.4 Public (Unauthenticated) Endpoints

Defined in `NoAuth.whiteList()` and `NoAuth.swagger()`:

```java
// Whitelist includes:
"/api/v1/customer/sign-up"
"/api/v1/customer/complete"
"/api/v1/service_provider/sign-up"
"/api/v1/service_provider/complete"
"/api/v1/auth/login"
"/api/v1/auth/refresh"
"/api/v1/auth/logout"
"/api/v1/user/forgot-password"
"/api/v1/user/reset-password"
"/api/v1/user/verify"
"/api/v1/user/activation-mail"
"/api/v1/user/reactivate"
"/api/v1/booking/execute-payment-paypal"
"/api/v1/booking/stripe-webhook"
"/api/v1/paypal/**"
"/api/v1/admin/login"

// Swagger:
"/swagger-ui/**"
"/v3/api-docs/**"
"/swagger-ui.html"
```

### 7.5 CORS

Currently configured wide-open for development:

```java
registry.addMapping("/**")
        .allowedMethods("*")
        .allowedOrigins("*");
```

> ⚠️ **Production note:** The stricter commented-out config (specific origins, credentials) should be activated before going to production.

---

## 8. Payment Integrations

### 8.1 Stripe

- **Mode:** Payment Intents API (server-side intent creation)
- **Flow:** `POST /api/v1/booking/payment-intent-stripe/{bookingId}` → returns `clientSecret` → frontend confirms via Stripe.js → webhook `POST /api/v1/booking/stripe-webhook` processes events
- **Config keys:** `stripe.api.secretKey`, `stripe.api.publicKey`
- **Stripe Customer:** Users get a `stripeId` on their `User` record; listings also get a `stripeId`

### 8.2 PayPal

- **Mode:** REST SDK (v1 Orders/Payments)
- **Mode:** `sandbox` (configurable via `paypal.mode`)
- **Flow:**
  1. `POST /api/v1/booking/authorize-paypal?bookingId=` → redirects customer to PayPal approval URL
  2. PayPal redirects to `GET /api/v1/paypal/success?paymentId=&payerId=`
  3. `executePaymentWithPaypal()` captures the payment
- **Cancel URL:** `GET /api/v1/paypal/cancel`
- **Platform fee:** Configurable `charges` property (default `0.15` = 15%)

---

## 9. Notification System

### 9.1 Email (Brevo)

- **API:** Brevo transactional SMTP v3
- **Trigger points:** Email verification after sign-up, password reset, booking notifications
- **Config:** `sendinblue.mail.api_key`, `sendinblue.mail.url`
- **Production guard:** `BrevoMailImpl` is `@Profile("!default")` — only active in non-default (e.g. `prod`) profile

### 9.2 SMS (Brevo)

- **API:** Brevo transactional SMS v3
- **Config:** `sendinblue.sms.url`

### 9.3 App Notifications (In-App)

- Persisted `AppNotification` entity linked to a `User` recipient
- Service providers retrieve their notifications via `GET /api/v1/service_provider/notifications`
- Full-text search enabled via `tsvector` column on the `app_notification` table

---

## 10. Database Schema

Managed by **Flyway** — single migration script `V1__db_setup.sql`.

### 10.1 Table Summary

| Table | Description |
|-------|-------------|
| `users` | Base user entity; roles stored as `varchar[]` |
| `address` | Reusable address (unit, street, suburb, state, postcode) |
| `customer` | Customer persona — FK to `users` |
| `service_provider` | Provider persona — FK to `users` + `user_identity` |
| `user_identity` | KYC document (id_type, id_number, id_image) |
| `listing` | Service listing; FK to `service_provider` + `address` |
| `listing_business_pictures` | Element collection for listing photo URLs |
| `booking` | Booking record; FK to `users` + `listing` + `booking_agreement` |
| `booking_book_dates` | Element collection for booked dates |
| `booking_agreement` | Terms/agreement for a booking |
| `task` | Customer-posted task |
| `task_task_dates` | Element collection for task dates |
| `category_name` | Top-level category label |
| `category` | Service category |
| `artezanServices` | Individual artezanService; FK to `category` |
| `task_hub_token` | JWT access + refresh token pairs |
| `task_hub_verification_token` | Email verification tokens |
| `change_password_token` | Password reset tokens |
| `password_reset_token` | Linked reset token with expiry |
| `app_notification` | In-app notification messages |

### 10.2 Full-Text Search Infrastructure

Each searchable table gets a `tsvector` computed column + GIN index + trigger:

| Table | Indexed Fields | Weights |
|-------|---------------|---------|
| `users` | first_name(A), last_name(B), phone_number(C), email_address(D), account_state(D) | — |
| `address` | suburb(A), post_code(B), state(C), street_name(D) | — |
| `user_identity` | id_type(A), id_number(B) | — |
| `listing` | service_name(A), service_category(B), service_description(C), business_name(D) | — |
| `task` | task_service_name(A), task_description(B), user_address(C) | — |
| `app_notification` | message | — |

**Extension:** `pg_trgm` is installed for trigram similarity (e.g. "mic" matches "Michael").

---

## 11. Configuration Reference

All config via `application.yml`. Values fall back to defaults when environment variables are absent.

### 11.1 Database

| Property | Env Var | Default |
|----------|---------|---------|
| `spring.datasource.url` | `DB_HOST`, `DB_PORT`, `DB_NAME` | `localhost:5432/art` |
| `spring.datasource.username` | — | `postgres` |
| `spring.datasource.password` | — | `@Tiptop2059!` |
| `spring.jpa.hibernate.ddl-auto` | — | `validate` (Flyway manages DDL) |

### 11.2 JWT

| Property | Env Var | Default |
|----------|---------|---------|
| `task.hub.secret.key` | `SECRET_KEY` | long base64 string |
| `access_expiration` | `ACCESS_EXPIRATION` | `3` (hours) |
| `refresh_expiration` | `REFRESH_EXPIRATION` | `168` (hours = 7 days) |

### 11.3 Notifications

| Property | Env Var | Default |
|----------|---------|---------|
| `sendinblue.mail.api_key` | `API_KEY` | `""` (empty — email disabled) |
| `sendinblue.mail.url` | `MAIL_URL` | Brevo SMTP endpoint |
| `sendinblue.sms.url` | `SMS_URL` | Brevo SMS endpoint |

### 11.4 Cloudinary

| Property | Env Var | Default |
|----------|---------|---------|
| `cloudinary.cloud.name` | `CLOUDINARY_NAME` | `ddgm9zdnr` |
| `cloudinary.api.secret` | `CLOUDINARY_SECRET` | (test secret) |
| `cloudinary.api.key` | `CLOUDINARY_KEY` | `249119262672761` |

### 11.5 Stripe

| Property | Env Var | Default |
|----------|---------|---------|
| `stripe.api.secretKey` | `SECRET.KEY` | Test secret key |
| `stripe.api.publicKey` | `PUBLISH.KEY` | Test publishable key |

### 11.6 PayPal

| Property | Env Var | Default |
|----------|---------|---------|
| `paypal.mode` | `MODE` | `sandbox` |
| `paypal.client.id` | `ID` | Sandbox client ID |
| `paypal.client.secret` | `SECRET` | Sandbox secret |
| `success.url` | `SUCCESS` | `https://artezanService-rppp.onrender.com/api/v1/paypal/success` |
| `cancel.url` | `CANCEL` | `https://artezanService-rppp.onrender.com/api/v1/paypal/cancel` |

### 11.7 Application

| Property | Env Var | Default |
|----------|---------|---------|
| `app.name` | `APP_NAME` | `Task Hub` |
| `app.email` | `APP_EMAIL` | `info@taskhub.com` |
| `frontend_url` | `FRONTEND_URL` | `https://taskhub-six.vercel.app` |
| `charges` | `CHARGES` | `0.15` (15% platform fee) |
| `oauth2.redirect.uri` | `OAUTH2.REDIRECT.URI` | `http://localhost:8080/login/oauth2/code/*` |

---

## 12. Seeded Data & Initialization

### 12.1 Flyway SQL Seed (`V1__db_setup.sql`)

On first run, the migration seeds:

- **Admin user:** `info@taskhub.com` / password `12345` (role: `ADMIN`)
- **15 artezanService categories** (Home Services, Personal Services, Events & Entertainments, etc.)
- **63 artezanService listings** mapped to those categories

### 12.2 `InitActors.java` (`@PostConstruct` — runs every startup)

| Method | Actor | Credentials |
|--------|-------|-------------|
| `creatAdmin()` | Admin re-sync | email: `info@taskhub.com`, password: `12345` (BCrypt-encoded if not already) |
| `createCustomer()` | Demo customer | email: `glory@gmail.com`, password: `@Bean1234` |
| `createServiceProvider()` | Demo provider | email: `chiamaka@gmail.com`, password: `@Bean1234` |

> ⚠️ **Security note:** These demo credentials and the hardcoded admin password **must be changed or removed before any production deployment**.

---

## 13. Running the Application

### 13.1 Prerequisites

| Tool | Version |
|------|---------|
| JDK | 21 |
| Maven | 3.9+ (or use included `./mvnw`) |
| PostgreSQL | 14+ |

### 13.2 Database Setup

```sql
-- Run as postgres superuser
CREATE DATABASE art;
-- Flyway will create all tables automatically on first boot
```

### 13.3 Environment Variables (optional overrides)

For local development, create a `.env` file in the project root (loaded by `java-dotenv`):

```dotenv
DB_HOST=localhost
DB_PORT=5432
DB_NAME=art
SECRET_KEY=your-256-bit-base64-secret
API_KEY=your-brevo-api-key
CLOUDINARY_NAME=your-cloud-name
CLOUDINARY_KEY=your-api-key
CLOUDINARY_SECRET=your-api-secret
```

### 13.4 Build & Run

```bash
# Run with Maven wrapper (development)
./mvnw spring-boot:run

# Or build and run the jar
./mvnw clean package -DskipTests
java -jar target/artezans-0.0.1-SNAPSHOT.jar
```

### 13.5 Run with Production Profile

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
# OR
java -jar target/artezans-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

With `--spring.profiles.active=prod` (any non-`default` value):
- `CloudinaryMultimediaServiceImpl` activates (real Cloudinary uploads)
- `BrevoMailImpl` activates (real email sending)

### 13.6 Swagger UI

Once running, navigate to:

```
http://localhost:8080/swagger-ui.html
```

All endpoints are documented with summaries, descriptions, and operation IDs. Bearer JWT authentication is pre-configured in the Swagger security scheme.

---

## 14. Spring Boot 4 Migration Notes

This project was upgraded from Spring Boot 3.x to **4.0.6** during active development. Key changes made:

| Issue | Root Cause | Fix Applied |
|-------|-----------|-------------|
| Flyway not running | `flyway-core` alone no longer triggers auto-config | Replaced with `spring-boot-starter-flyway` |
| `ObjectMapper` injection failures | Spring Boot 4 defaults to `tools.jackson` (Jackson 3); legacy code uses `com.fasterxml.jackson` | Added explicit `com.fasterxml.jackson.databind.ObjectMapper` `@Bean` in `AppConfig` |
| Dual-bean conflicts at startup | `MultimediaService` and `MailService` each had 2 implementations | Added `@Profile("!default")` to production impls |
| Security `IllegalArgumentException` on matchers | Spring Security 7 requires matchers to start with `/` | Prefixed all paths in `NoAuth.whiteList()` |
| `@Scheduled` methods silently not running | Spring Framework 7 cannot proxy `private` `@Scheduled` methods | Changed all `@Scheduled` methods from `private` to package-private |

---

## 15. Known Constraints & Future Work

| Area | Issue / Opportunity |
|------|-------------------|
| **CORS** | Currently `allowedOrigins("*")` — should be locked to frontend URL in production |
| **Error codes** | `UserNotFoundException` / `UserNotAuthorizedException` return HTTP 400 instead of 404/403 |
| **Admin script endpoint** | `POST /api/v1/admin/script` has no `@PreAuthorize` guard — dangerous in production |
| **Demo credentials** | `InitActors` seeds plaintext-equivalent dev passwords; must be removed or ENV-controlled |
| **OAuth2** | OAuth2 login infrastructure is scaffolded but success handler and `userService` are commented out |
| **Listing `@PreAuthorize`** | `create-listing` has its auth check commented out — re-enable when ready |
| **Jackson 2/3 coexistence** | The legacy `ObjectMapper` bridge is a workaround; long-term migrate all serialization to the native Jackson 3 path |
| **Token cleanup** | `@Scheduled` token expiry jobs run in-process; consider moving to a dedicated cron or database function for scale |
| **Integration tests** | Test profile uses stub impls (multimedia, mail) — integration test suite needs to be built out |
| **`application-host.yml`** | A secondary host profile config exists — purpose/usage should be documented |
