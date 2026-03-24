# ExpenseTrackerV2

A comprehensive expense tracking application built with Spring Boot, designed to help users manage their personal finances by tracking expenses, categories, and reserve funds.

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [API Endpoints](#api-endpoints)
- [Configuration](#configuration)
- [Rate Limiting](#rate-limiting)
- [Database](#database)
- [Security](#security)
- [Setup Instructions](#setup-instructions)
- [Current Status](#current-status)
- [Known Issues](#known-issues)
- [Future Enhancements](#future-enhancements)

## Features
- **User Authentication**: JWT-based authentication with login, register, refresh token, and logout functionality.
- **Expense Management**: Add, list, and filter expenses by date and category with pagination.
- **Category Management**: Create, update, delete, and list expense categories.
- **Reserve Funds**: Manage reserved money for future expenses.
- **Income Tracking**: Add income to user balance.
- **Rate Limiting**: Configurable rate limiting per endpoint using Bucket4j.
- **Security**: Spring Security with custom JWT filters and role-based access.

## Tech Stack
- **Backend**: Spring Boot 3.5.8
- **Language**: Java 21
- **Database**: PostgreSQL (dev), H2/MySQL (test/prod options)
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security, JWT (JJWT)
- **Rate Limiting**: Bucket4j with Caffeine cache
- **Build Tool**: Gradle
- **Other**: Lombok, Validation, DevTools

## API Endpoints

### Authentication (`/api/auth`)
- `POST /login` - User login
- `POST /register` - User registration
- `POST /refresh` - Refresh JWT token
- `POST /logout` - User logout
- `POST /income` - Add income to balance
- `GET /balance` - Get user balance

### Expenses (`/api/expense`)
- `POST /` - Add new expense
- `GET /{page}` - List expenses with pagination
- `GET /date` - List expenses by date range
- `GET /category` - List expenses by category and date

### Categories (`/api/category`)
- `PATCH /` - Update category name
- `DELETE /` - Delete category
- `GET /list` - List user categories

### Reserve Funds (`/api/reserve`)
- `POST /` - Add reserve fund
- `DELETE /` - Delete reserve fund
- `GET /` - List reserve funds
- `PUT /` - Update reserve fund

## Configuration

### Application Properties
- `application.properties`: Base configuration including app name, JPA DDL auto, JWT secret.
- `application-dev.properties`: Development config with PostgreSQL database, SQL logging enabled.
- `application-prod.properties`: Production config with SQL logging disabled.

### Environment Variables
- `JWT_SECRET`: Secret key for JWT token generation (must be at least 32 bytes).

### Profiles
- `dev`: Development profile with detailed logging.
- `prod`: Production profile with minimal logging.

## Rate Limiting

Rate limiting is configured via `application.yml` and implemented using Bucket4j with Caffeine cache.

### Configuration Structure
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
    # ... other endpoints
```

### Key Types
- `IP`: Rate limit based on client IP address.
- `USER`: Rate limit based on authenticated user ID.

### Endpoints Configuration
- **LOGIN**: 5 requests per minute per IP
- **REGISTER**: 3 requests per minute per IP
- **REFRESH**: 10 requests per minute per user
- **LOGOUT**: 10 requests per minute per user
- **API**: 60 requests per minute per user for all `/api/**` endpoints

## Database

### Schema
The application uses JPA with Hibernate for ORM. DDL auto is set to `validate` in production, requiring manual schema management.

### Supported Databases
- PostgreSQL (primary for dev)
- H2 (for testing)
- MySQL (alternative)

### Models
- `User`: User account information
- `Expense`: Expense records
- `Category`: Expense categories
- `RefreshToken`: JWT refresh tokens
- `ReservedFund`: Reserved money entries

## Security

### Authentication
- JWT tokens with access and refresh token mechanism.
- Refresh tokens stored in database for validation.
- Password encoding using Spring Security's default encoder.

### Authorization
- All API endpoints require authentication except login and register.
- User-specific data isolation using `CurrentUserProvider`.

### Filters
- `JwtAuthenticationFilter`: Validates JWT tokens on requests.
- `RateLimitInterceptor`: Applies rate limiting before request processing.

## Setup Instructions

1. **Prerequisites**:
   - Java 21
   - Gradle
   - PostgreSQL (for dev)

2. **Clone and Build**:
   ```bash
   git clone <repository-url>
   cd ExpenseTrackerV2/my-project
   ./gradlew build
   ```

3. **Database Setup**:
   - Create PostgreSQL database: `expense_tracker`
   - Update credentials in `application-dev.properties` if needed.

4. **Run**:
   ```bash
   ./gradlew bootRun
   ```
   Or for specific profile:
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

5. **Test**:
   ```bash
   ./gradlew test
   ```

## Current Status

### Completed Features
- User registration and authentication with JWT
- Expense CRUD operations with pagination and filtering
- Category management
- Reserve fund management
- Rate limiting implementation
- Basic security configuration
- Database integration with PostgreSQL

### Recent Changes
- Implemented rate limiting using Bucket4j
- Added refresh token functionality
- Configured multiple database support
- Added comprehensive logging

### Project Structure
```
src/main/java/com/myApp/ExpenseTracker/
├── Config/          # Configuration classes
├── Controller/      # REST controllers
├── Dto/             # Data transfer objects
├── Exception/       # Custom exceptions
├── Model/           # JPA entities
├── Ratelimit/       # Rate limiting components
├── Repository/      # Data access layer
├── Req/             # Request DTOs
├── Service/         # Business logic
└── Utils/           # Utility classes
```

## Known Issues

None currently identified. All previously noted issues have been resolved.

## Future Enhancements

- Add expense analytics and reporting
- Implement email notifications
- Add multi-currency support
- Enhance UI with frontend framework
- Add API documentation with Swagger
- Implement caching for better performance
- Add unit and integration tests coverage
- Containerize with Docker
- Add CI/CD pipeline

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes and add tests
4. Submit a pull request

## License

This project is licensed under the MIT License.
