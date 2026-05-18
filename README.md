# ExpenseTrackerV2

A comprehensive, production-ready personal finance tracking backend built with Spring Boot. Track expenses, manage categories, reserve funds for future goals, and maintain complete audit logs—all with JWT authentication and configurable rate limiting.

## Table of Contents

- [Project Summary](#project-summary)
- [Purpose & Problem Solved](#purpose--problem-solved)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
  - [Layers](#layers)
  - [Key Components](#key-components)
- [Features](#features)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
  - [User](#user)
  - [Expense](#expense)
  - [Category](#category)
  - [Reserved](#reserved)
  - [RefreshToken](#refreshtoken)
- [Configuration & Environment Variables](#configuration--environment-variables)
  - [Application Properties & Profiles](#application-properties--profiles)
  - [Required Environment Variables](#required-environment-variables)
  - [Rate Limiting Configuration](#rate-limiting-configuration)
- [Error Handling & Logging](#error-handling--logging)
- [Known Limitations](#known-limitations)
- [Troubleshooting & Common Issues](#troubleshooting--common-issues)
  - [Rate-limit Binding Error](#rate-limit-binding-error)
  - [Reserve Not Found / Label Normalization](#reserve-not-found--label-normalization)
  - [403 vs 401 on Token Expiry](#403-vs-401-on-token-expiry)
  - [405 Method Not Allowed](#405-method-not-allowed)
- [Usage & Examples](#usage--examples)
  - [Build & Run (PowerShell)](#build--run-powershell)
  - [API Examples (curl)](#api-examples-curl)
- [Project Structure](#project-structure)
- [Tests & Verification](#tests--verification)
- [Current Status & Changelog](#current-status--changelog)
- [Future Improvements](#future-improvements)
  - [Reporting & Analytics](#reporting--analytics)
  - [Role-Based Access Control (RBAC)](#role-based-access-control-rbac)
  - [Additional Enhancements](#additional-enhancements)
- [Contributing](#contributing)
- [License](#license)

---

## Project Summary

**ExpenseTrackerV2** is a backend service (Spring Boot 3.x) for personal finance tracking. It enables users to:
- Register and securely authenticate via JWT tokens
- Track incomes and expenses with categorization
- Reserve funds for future planned spending
- Monitor spending patterns with audit logs
- Access the service from mobile (Flutter) or web clients

The service emphasizes **security**, **auditability**, and **rate limiting** to prevent abuse while maintaining a clean, layered REST API.

---

## Purpose & Problem Solved

### Why This Project?

Managing personal finances requires a secure, reliable backend that:
- Keeps user data private and isolated
- Prevents unauthorized access via JWT + refresh tokens
- Prevents API abuse via rate limiting
- Provides an audit trail for compliance and debugging
- Supports future analytics and reporting

### What Problems Does It Solve?

1. **Expense Tracking**: Users easily record and categorize spending.
2. **Fund Reservation**: Set aside money for planned expenses (e.g., vacation, emergency fund).
3. **Authentication**: Secure login/logout with token-based architecture.
4. **Rate Limiting**: Protect endpoints from brute-force or excessive API usage.
5. **Audit Logging**: Track who created/updated/deleted financial records for compliance.
6. **User Isolation**: Each user only sees their own data.

---

## Tech Stack

| Component          | Technology                                       | Version             |
|--------------------|--------------------------------------------------|---------------------|
| **Language**       | Java                                             | 21                  |
| **Framework**      | Spring Boot                                      | 3.5.8               |
| **ORM**            | Spring Data JPA / Hibernate                      | Latest (via Spring) |
| **Authentication** | Spring Security + JJWT                           | JJWT 0.12.5         |
| **Database**       | PostgreSQL (dev), H2 (test), MySQL (alternative) | Latest              |
| **Rate Limiting**  | Bucket4j + Caffeine                              | 8.10.1 / 3.1.8      |
| **Build Tool**     | Gradle                                           | Latest (wrapper)    |
| **Utilities**      | Lombok, Jakarta Validation                       | Latest              |
| **Logging**        | SLF4J + Logback                                  | Latest              |

---

## Architecture

### Layers

The application follows a **layered architecture** organized by concerns:

```
Controllers (REST endpoints)
    ↓
Services (Business logic & transactions)
    ↓
Repositories (Data access layer)
    ↓
Models (JPA entities / Domain)
```

**Detailed Layer Breakdown:**

- **Controllers** (`com.myApp.ExpenseTracker.Controller`)
  - REST endpoints for clients (Flutter, web, etc.)
  - Request validation and response mapping
  - Examples: `ReserveFundController`, `ExpenseController`, `AuthController`

- **Services** (`com.myApp.ExpenseTracker.Service`)
  - Business logic and transaction management
  - Examples: `ReservedService`, `UserService`, `JwtService`, `AuditService`
  - Handles reserve operations (deposit, withdraw, create), user operations, and JWT token generation/parsing

- **Repositories** (`com.myApp.ExpenseTracker.Repository`)
  - Spring Data JPA interfaces for database access
  - Custom query methods (e.g., `findByUser_IdAndLabel`)
  - Examples: `ReservedRepository`, `UserRepository`, `ExpenseRepository`

- **Models** (`com.myApp.ExpenseTracker.Model`)
  - JPA entities mapping to database tables
  - Examples: `User`, `Expense`, `Category`, `Reserved`, `RefreshToken`

- **Configuration** (`com.myApp.ExpenseTracker.Config`)
  - Security configuration (`SecurityConfig`, `JwtAuthenticationFilter`)
  - Rate-limiting configuration (`RateLimitProperties`, `EndpointConfig`)
  - Application beans and filters

- **Exception Handling** (`com.myApp.ExpenseTracker.Exeception`)
  - Custom exceptions (`BusinessException`, `AuthException`, `ResourceNotFoundException`, etc.)
  - `GlobalExceptionHandler` for centralized error responses

- **Rate Limiting** (`com.myApp.ExpenseTracker.Ratelimit`)
  - Bucket4j + Caffeine implementation
  - Per-endpoint request throttling

- **Utilities** (`com.myApp.ExpenseTracker.Utils`)
  - Enums and helper classes (e.g., `KeyType` for rate-limit key types)

### Key Components

#### Authentication & Authorization

```
Request with Bearer token
    ↓
JwtAuthenticationFilter (parses token)
    ↓
JwtService.extractUsername / extractUserid
    ↓
SecurityContext set with user authorities
    ↓
(Optional) Rate limit check
    ↓
Protected endpoint / business logic
```

- **SecurityConfig**: Configures stateless sessions, permit-all endpoints (/api/auth/login, /register, /refresh), and adds JWT filter before `UsernamePasswordAuthenticationFilter`.
- **JwtAuthenticationFilter**: Parses Authorization header, validates token, loads user details, and sets `SecurityContext`.
- **JwtService**: Generates and parses JWT access/refresh tokens, extracts claims (username, userid).

#### Rate Limiting

```
Request → Rate Limit Interceptor/Filter
  ↓
Check Bucket4j token bucket (per IP or User)
  ↓
If tokens available: allow request
  ↓
If no tokens: return 429 Too Many Requests
```

- **RateLimitProperties**: Loads `rate-limit` config from `application.yml`.
- **EndpointConfig**: Maps endpoint paths to rate-limit rules (capacity, refill rate, key type).

#### Error Handling

```
Any exception thrown in service/controller
    ↓
GlobalExceptionHandler catches it
    ↓
Maps to appropriate HTTP status + ErrorResponse DTO
    ↓
Returns to client (e.g., 400 Bad Request, 404 Not Found, 500 Internal Server Error)
```

---

## Features

✅ **Completed & Implemented**

- **JWT Authentication**
  - Login and register endpoints
  - Access token (short-lived) + refresh token (long-lived, persisted)
  - Logout (token revocation via database check)

- **Expense Management**
  - Create, read, list, filter expenses
  - Filter by date range and category
  - Pagination support

- **Category Management**
  - Create, update (patch), delete, list categories
  - User-scoped (each user has their own categories)

- **Reserve Funds**
  - Create reserves with label, note, and initial amount
  - Deposit money to reserves
  - Withdraw money from reserves
  - Update reserve details (label, note)
  - Delete reserves
  - View total reserved amount and list all reserves

- **Income Tracking**
  - Add income to user balance

- **Rate Limiting**
  - Per-endpoint configurable limits
  - IP-based limiting for public endpoints (login, register)
  - User-based limiting for authenticated endpoints
  - Configurable capacity, refill tokens, and refill duration

- **Audit Logging**
  - Log all critical operations (create, update, delete) via `AuditService`
  - Track user actions and changes for compliance

- **Security**
  - Password encoding via BCrypt
  - User data isolation (users only access their own data)
  - Centralized exception handling with consistent error responses

---

## API Endpoints

### Authentication (`/api/auth`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|----------------|
| `POST` | `/api/auth/login` | Login with username/password, returns access + refresh token | ❌ No |
| `POST` | `/api/auth/register` | Register new user account | ❌ No |
| `POST` | `/api/auth/refresh` | Refresh access token using refresh token | ❌ No |
| `POST` | `/api/auth/logout` | Logout and revoke refresh token | ✅ Yes |
| `POST` | `/api/auth/income` | Add income to user balance | ✅ Yes |
| `GET` | `/api/auth/balance` | Get current user balance | ✅ Yes |

### Expenses (`/api/expense`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|----------------|
| `POST` | `/api/expense` | Create new expense | ✅ Yes |
| `GET` | `/api/expense/{page}` | List expenses with pagination | ✅ Yes |
| `GET` | `/api/expense/date` | List expenses by date range | ✅ Yes |
| `GET` | `/api/expense/category` | List expenses by category and date | ✅ Yes |

### Categories (`/api/category`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|----------------|
| `PATCH` | `/api/category` | Update category name | ✅ Yes |
| `DELETE` | `/api/category` | Delete category | ✅ Yes |
| `GET` | `/api/category/list` | List all user categories | ✅ Yes |

### Reserve Funds (`/api/reserve`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|----------------|
| `POST` | `/api/reserve` | Create new reserve fund | ✅ Yes |
| `DELETE` | `/api/reserve/{reserve_id}` | Delete reserve fund | ✅ Yes |
| `GET` | `/api/reserve` | List all reserves for user | ✅ Yes |
| `PUT` | `/api/reserve` | Update reserve label/note | ✅ Yes |
| `POST` | `/api/reserve/deposite` | Deposit money to reserve (⚠️ note spelling) | ✅ Yes |
| `POST` | `/api/reserve/withdraw` | Withdraw money from reserve | ✅ Yes |
| `GET` | `/api/reserve/balance` | Get total reserved amount | ✅ Yes |


---

## Database Schema

All entities use Spring Data JPA with Hibernate ORM. Database-specific details (trigger-based timestamps, etc.) are configured in `application*.properties`.

### User

| Column       | Type            | Constraints          | Description            |
|--------------|-----------------|----------------------|------------------------|
| `USER_ID`    | `BIGINT`        | PK, AUTO_INCREMENT   | User unique identifier |
| `NAME`       | `VARCHAR(255)`  | NOT NULL             | User full name         |
| `USERNAME`   | `VARCHAR(255)`  | NOT NULL, UNIQUE     | Login username         |
| `PASSWORD`   | `VARCHAR(255)`  | NOT NULL             | BCrypt hashed password |
| `EMAIL`      | `VARCHAR(255)`  | NOT NULL, UNIQUE     | User email             |
| `CREATED_AT` | `TIMESTAMP`     | NOT NULL, DB-managed | Account creation date  |
| `BALANCE`    | `DECIMAL(18,2)` | NOT NULL, DEFAULT 0  | User account balance   |

**Entity**: `com.myApp.ExpenseTracker.Model.User`

### Expense

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `EXP_ID` | `BIGINT` | PK, AUTO_INCREMENT | Expense unique identifier |
| `AMOUNT` | `DECIMAL(18,2)` | NOT NULL | Expense amount |
| `EXPENSE_DATE` | `DATE` | NOT NULL | Date of expense |
| `CREATED_AT` | `TIMESTAMP` | NOT NULL, DB-managed | Record creation date |
| `NOTE` | `TEXT` | Nullable | Optional expense note/description |
| `CATEGORY_ID` | `BIGINT` | FK → CATEGORY.CATID | Category reference |
| `USER_ID` | `BIGINT` | FK → USERS.USER_ID | User who incurred expense |

**Entity**: `com.myApp.ExpenseTracker.Model.Expense`

### Category

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `CATID` | `BIGINT` | PK, AUTO_INCREMENT | Category unique identifier |
| `NAME` | `VARCHAR(255)` | NOT NULL | Category name (lowercased) |
| `USER_ID` | `BIGINT` | FK → USERS.USER_ID, NOT NULL | User who owns category |

**Entity**: `com.myApp.ExpenseTracker.Model.Category`

### Reserved

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `ID` | `BIGINT` | PK, AUTO_INCREMENT | Reserve fund unique identifier |
| `USER_ID` | `BIGINT` | FK → USERS.USER_ID, NOT NULL | User who owns reserve |
| `LABEL` | `VARCHAR(255)` | NOT NULL | Reserve label/name (lowercased) |
| `NOTE` | `TEXT` | Nullable | Optional reserve description |
| `AMOUNT` | `DECIMAL(18,2)` | NOT NULL | Reserved amount |

**Entity**: `com.myApp.ExpenseTracker.Model.Reserved`

### RefreshToken

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `ID` | `BIGINT` | PK, AUTO_INCREMENT | Token record unique identifier |
| `TOKEN` | `VARCHAR(500+)` | NOT NULL, UNIQUE | JWT refresh token value |
| `EXPIRY_DATE` | `TIMESTAMP` | NOT NULL | Token expiration time |
| `CREATED_AT` | `TIMESTAMP` | NOT NULL, DEFAULT NOW | Token creation time |
| `USER_ID` | `BIGINT` | FK → USERS.USER_ID, NOT NULL, UNIQUE | User who owns token |

**Entity**: `com.myApp.ExpenseTracker.Model.RefreshToken`

---

## Configuration & Environment Variables

### Application Properties & Profiles

The application supports multiple profiles:
- **dev** (`application-dev.properties`): PostgreSQL, SQL logging enabled
- **prod** (`application-prod.properties`): Production settings, SQL logging disabled

### Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `jwt.secret` or `JWT_SECRET` | Secret key for HMAC-SHA256 token signing (≥ 32 bytes recommended) | `mySecretKey1234567890123456789` |
| `spring.profiles.active` | Active profile (dev/prod) | `dev` |

### Rate Limiting Configuration

Located in `src/main/resources/application.yml`:

```yaml
rate-limit:
  enabled: true
  endpoints:
    - name: LOGIN
      paths:
        - /api/auth/login
      key-type: IP
      capacity: 5
      refill-tokens: 5
      refill-duration: 60  # seconds

    - name: REGISTER
      paths:
        - /api/auth/register
      key-type: IP
      capacity: 3
      refill-tokens: 3
      refill-duration: 60

    - name: REFRESH
      paths:
        - /api/auth/refresh
      key-type: USER
      capacity: 10
      refill-tokens: 10
      refill-duration: 60

    - name: LOGOUT
      paths:
        - /api/auth/logout
      key-type: USER
      capacity: 10
      refill-tokens: 10
      refill-duration: 60

    - name: API
      paths:
        - /api/**
      key-type: USER
      capacity: 60
      refill-tokens: 60
      refill-duration: 60
```

**Key Types:**
- **IP**: Rate limit based on client IP address (public endpoints like login/register).
- **USER**: Rate limit based on authenticated user ID (private endpoints).

**Configuration Binding:**
- Class: `com.myApp.ExpenseTracker.Config.RateLimitProperties`
- Prefix: `rate-limit`

---

## Error Handling & Logging

### Centralized Exception Handling

All exceptions are caught and processed by `GlobalExceptionHandler` (`com.myApp.ExpenseTracker.Exeception.GlobalExceptionHandler`).

**Exception Mapping:**

| Exception Class                    | HTTP Status               | Example Scenario                           |
|------------------------------------|---------------------------|--------------------------------------------|
| `AuthException`                    | Status from exception     | Invalid/expired token, auth failure        |
| `BusinessException` (+ subclasses) | Status from exception     | Resource not found, already exists         |
| `MethodArgumentNotValidException`  | 400 Bad Request           | Invalid request body fields                |
| `Exception` (fallback)             | 500 Internal Server Error | Unexpected error (logged with stack trace) |

**Error Response Format:**

```json
{
  "status": 400,
  "message": "Invalid request",
  "path": "/api/reserve",
  "timestamp": "2026-05-18T10:30:00Z"
}
```

### Custom Exceptions

- **ResourceNotFoundException**: 404 when entity not found
- **ResourceAlreadyExists**: 409 when duplicate detected
- **InsufficientBalanceException**: 400 insufficient funds
- **InvalidTokenException**: 401 invalid/malformed token
- **TokenExpiredException**: 401 token expired

### Logging Configuration

- **Logger**: SLF4J + Logback
- **Levels**:
  - Controllers & services log at `INFO` level for request/response flow
  - Errors logged at `WARN` and `ERROR` levels
  - SQL logging can be enabled in `application-dev.properties`:
    ```properties
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true
    ```
- **Audit Logging**: `AuditService` logs business-critical operations (create, update, delete reserves, expenses, etc.)

---

## Known Limitations

1. **Rate-limiting Binding**: If Lombok annotation processing is disabled, Spring may report "No setter found for property 'enabled'" in `RateLimitProperties`.

2. **CORS Configuration**: CORS handling may need explicit configuration if frontend runs on a different origin.

5. **No Role-Based Access Control (RBAC)**: Currently, all authenticated users have the same privileges. No admin/user role distinction yet.

6. **No Report Generation**: Analytics and report generation not yet implemented (planned for future).

7. **Test Coverage**: Unit and integration test coverage is limited. More tests recommended before production deployment.

8. **Performance**: No caching layer implemented (e.g., Caffeine/Redis for frequent queries like user summaries).

---

## Troubleshooting & Common Issues

### Rate-limit Binding Error

**Symptom:**
```
Failed to bind properties under 'rate-limit' to com.myApp.ExpenseTracker.Config.RateLimitProperties:
    Property: rate-limit.enabled
    Reason: java.lang.IllegalStateException: No setter found for property: enabled
```

**Root Cause:**
- Lombok annotation processing is disabled or misconfigured.
- IDE/build system not running annotation processor during compilation.

**Quick Fix (Option 1):**
Add an explicit setter to `RateLimitProperties.java`:
```java
public void setEnabled(boolean enabled) {
    this.enabled = enabled;
}
```

**Quick Fix (Option 2):**
- Ensure Lombok is in `build.gradle` (already present).
- In your IDE (IntelliJ):
  - Go to Settings → Build, Execution, Deployment → Compiler → Annotation Processors
  - Enable "Enable annotation processing"
  - Rebuild project

---

### 405 Method Not Allowed

**Symptom:**
```
Response: 405 Method Not Allowed
```

**Root Cause:**
- Request uses wrong HTTP method for endpoint (e.g., GET instead of POST).

**Fix:**
- Verify endpoint mapping in controller:
  - Reserve deposit is `@PostMapping("/deposite")` not `@GetMapping`
  - Some endpoints are `@PutMapping` (update), `@DeleteMapping` (delete)
- Ensure client sends correct HTTP verb

---

## Usage & Examples

### Build & Run (PowerShell)

**Build:**
```powershell
cd D:\PROJECTS\ExpenseTrackerV2\my-project
./gradlew build
```

**Run (dev profile):**
```powershell
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Run JAR:**
```powershell
java -jar build/libs/my-project-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

**Run tests:**
```powershell
./gradlew test
```

### API Examples (curl)

**Register:**
```powershell
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "name": "John Doe",
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

**Login:**
```powershell
$response = curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "username": "johndoe",
    "password": "SecurePass123"
  }' | ConvertFrom-Json
$token = $response.accessToken
```

**Add Income:**
```powershell
curl -X POST http://localhost:8080/api/auth/income `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "amount": 5000.00
  }'
```

**Create Reserve:**
```powershell
curl -X POST http://localhost:8080/api/reserve `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "label": "vacation",
    "note": "Summer trip fund",
    "amount": 1000.00
  }'
```

**Deposit to Reserve:**
```powershell
curl -X POST http://localhost:8080/api/reserve/deposite `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "label": "vacation",
    "amount": 200.00
  }'
```

**List Reserves:**
```powershell
curl -X GET http://localhost:8080/api/reserve `
  -H "Authorization: Bearer $token"
```

**Create Expense:**
```powershell
curl -X POST http://localhost:8080/api/expense `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "amount": 50.00,
    "expenseDate": "2026-05-18",
    "categoryId": 1,
    "note": "Groceries"
  }'
```

**Refresh Token:**
```powershell
curl -X POST http://localhost:8080/api/auth/refresh `
  -H "Content-Type: application/json" `
  -d '{
    "refreshToken": "<YOUR_REFRESH_TOKEN>"
  }'
```

---

## Project Structure

```
src/main/java/com/myApp/ExpenseTracker/
├── Config/
│   ├── SecurityConfig.java                 # Security filter chain
│   ├── JwtAuthenticationFilter.java        # JWT token validation
│   ├── JwtAuthenticationEntryPoint.java    # 401 response handler
│   ├── JwtAccessDeniedHandler.java         # 403 response handler
│   ├── RateLimitProperties.java            # Rate-limit config binding
│   ├── EndpointConfig.java                 # Endpoint rate-limit rules
│   ├── AsyncConfig.java                    # Async configuration
│   └── EndpointResolver.java               # Endpoint path resolution
├── Controller/
│   ├── AuthController.java                 # Login, register, refresh, logout
│   ├── ReserveFundController.java          # Reserve CRUD & operations
│   ├── ExpenseController.java              # Expense CRUD
│   └── CategoryController.java             # Category CRUD
├── Service/
│   ├── JwtService.java                     # Token generation & parsing
│   ├── UserService.java                    # User operations
│   ├── ReservedService.java                # Reserve fund logic
│   ├── ExpenseService.java                 # Expense operations
│   ├── CategoryService.java                # Category operations
│   ├── AuditService.java                   # Audit logging
│   ├── CurrentUserProvider.java            # Get current authenticated user
│   ├── RefreshTokenService.java            # Refresh token management
│   └── CustomUserDetailsService.java       # Spring Security user details
├── Repository/
│   ├── UserRepository.java
│   ├── ReservedRepository.java
│   ├── ExpenseRepository.java
│   ├── CategoryRepository.java
│   ├── RefreshTokenRepository.java
│   └── AuditRepository.java
├── Model/
│   ├── User.java
│   ├── Expense.java
│   ├── Category.java
│   ├── Reserved.java
│   ├── RefreshToken.java
│   ├── Audit.java
│   └── CustomUserDetails.java
├── Dto/
│   ├── ErrorResponse.java
│   ├── ReservedResponse.java
│   ├── LoginResponse.java
│   └── ... (other response DTOs)
├── Req/
│   ├── ReservedRequest.java
│   ├── ReservedMoneyRequest.java
│   ├── UpdateReserveRequest.java
│   └── ... (other request DTOs)
├── Exeception/
│   ├── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   ├── AuthException.java
│   ├── ResourceNotFoundException.java
│   ├── ResourceAlreadyExists.java
│   ├── InvalidTokenException.java
│   ├── TokenExpiredException.java
│   └── InsufficientBalanceException.java
├── Ratelimit/
│   ├── RateLimitInterceptor.java
│   └── RateLimitingFilter.java
├── Utils/
│   ├── KeyType.java                        # Enum: IP, USER
│   └── EntityType.java                     # Enum for audit logs
└── Main.java                               # Spring Boot entry point

src/main/resources/
├── application.yml                         # Rate-limit config
├── application.properties                  # Base config
├── application-dev.properties              # Dev config
└── application-prod.properties             # Prod config
```

---

## Tests & Verification

### Manual Testing Checklist

- [ ] **Authentication**
  - [ ] Register new user
  - [ ] Login and receive access + refresh tokens
  - [ ] Use access token to call protected endpoint
  - [ ] Refresh expired access token
  - [ ] Logout and verify refresh token is revoked

- [ ] **Reserves**
  - [ ] Create reserve with label
  - [ ] Deposit money to reserve
  - [ ] Withdraw money from reserve
  - [ ] List reserves
  - [ ] Update reserve label/note
  - [ ] Delete reserve
  - [ ] Verify label case-insensitivity (try "Trip", "TRIP", "trip")

- [ ] **Expenses & Categories**
  - [ ] Create category
  - [ ] Create expense with category
  - [ ] List expenses with pagination
  - [ ] Filter expenses by date range
  - [ ] Update and delete category

- [ ] **Rate Limiting**
  - [ ] Hit login endpoint > 5 times from same IP → expect 429
  - [ ] Hit authenticated endpoint > 60 times per minute → expect 429

- [ ] **Error Handling**
  - [ ] Invalid token → 401
  - [ ] Expired token → 401
  - [ ] Missing Authorization header → 401
  - [ ] Non-existent resource (id) → 404
  - [ ] Duplicate resource (e.g., category name) → 409
  - [ ] Invalid request body → 400 with field errors

### Recommended Test Setup

- Enable SQL logging during development (`application-dev.properties`).
- Use a REST client (Postman, curl, VS Code REST Client, etc.).
- Test with both dev and prod profiles.
- Verify database state directly after operations.

---

## Current Status & Changelog

### Version 0.0.1-SNAPSHOT (Current)

**Completed:**
- ✅ JWT authentication with access + refresh tokens
- ✅ User registration, login, logout, balance management
- ✅ Expense CRUD and filtering
- ✅ Category management (user-scoped)
- ✅ Reserve fund full lifecycle (create, deposit, withdraw, update, delete)
- ✅ Rate limiting per endpoint (IP and USER key types)
- ✅ Audit logging for critical operations
- ✅ Centralized error handling with custom exceptions
- ✅ Spring Security configuration with JWT filter
- ✅ Database schema with PostgreSQL / H2 / MySQL support

**In Progress / Planned:**
- 🔄 Report generation (monthly summaries, CSV/PDF export)
- 🔄 Role-based access control (admin, user roles)
- 🔄 Analytics and spending trends
- 🔄 Email notifications
- 🔄 Improved test coverage
- 🔄 API documentation (Swagger/OpenAPI)
- 🔄 Docker containerization
- 🔄 Performance optimization (caching, query optimization)

---

## Future Improvements

### Reporting & Analytics

- Monthly and weekly expense summaries
- Category-wise spending breakdown
- Trend analysis (e.g., "spending increased 10% this month")
- Savings goals tracking
- Budget alerts when exceeding limits
- CSV and PDF report export

### Role-Based Access Control (RBAC)

- Add `role` column to `User` entity (ADMIN, USER)
- Implement role-based authority checks in `SecurityConfig`
- Allow admins to view system-wide analytics
- Restrict sensitive operations to admins

### Additional Enhancements

- **Notifications**: Email alerts for transactions, low balance, monthly summaries
- **Multi-Currency Support**: Track expenses in different currencies with conversion
- **Shared Expenses**: Allow splitting expenses among multiple users
- **Scheduled Tasks**: Automatic monthly reports, balance reconciliation
- **Caching**: Redis/Caffeine for frequent queries (user balance, summary totals)
- **API Documentation**: Swagger/OpenAPI for auto-generated interactive docs
- **CI/CD Pipeline**: GitHub Actions or Jenkins for automated build, test, deploy
- **Microservices Ready**: Structure for extraction into separate services (auth, expense, reporting)

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m "Add my feature"`)
4. Push to branch (`git push origin feature/my-feature`)
5. Open a Pull Request

Please include unit tests and update the README if needed.

---

## License

This project is licensed under the MIT License. See the LICENSE file for details.

---

**Questions or Issues?**

If you encounter problems or have suggestions:
1. Check the [Troubleshooting](#troubleshooting--common-issues) section above.
2. Review the database schema and API endpoint mappings.
3. Enable SQL and debug logs for deeper investigation.
4. Open an issue on GitHub or contact maintainers.

---

*Last Updated: May 18, 2026*
*Status: Production-Ready (v0.0.1-SNAPSHOT)*

