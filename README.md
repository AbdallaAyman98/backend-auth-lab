# Java Auth Backend

A production-style authentication backend built from scratch in raw Java no Spring, no frameworks. Built to understand how auth systems actually work under the hood.

---

## Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| HTTP Server | `com.sun.net.httpserver` |
| Database | MySQL 8+ |
| Connection Pool | HikariCP |
| Cache / Sessions | Redis (Redis Cloud) |
| Password Hashing | Argon2id (`argon2-jvm`) |
| Token | JWT (`java-jwt`) |
| JSON | Jackson |

---

## Architecture

```
Handler → Validator → Service → Repository → MySQL
                                           → Redis (tokens)
```

Each layer has one responsibility:

- **Handler** — HTTP only. Reads request, calls service, sends response.
- **Validator** — Format and security checks. Returns `ValidationResult`.
- **Service** — Business logic. Throws typed exceptions.
- **Repository** — SQL queries only. Never touches business logic.

---

## Project Structure

```
src/
│
├── configs/
│   ├── AppConfig.java              # App-level config (JWT secret, port, etc.)
│   └── DatabaseConfig.java         # DB connection properties
│
├── db/
│   ├── DBConnectionPool.java       # HikariCP datasource singleton
│   ├── HikariCPProperties.java     # HikariCP pool configuration
│   └── RedisConnectionPool.java    # Jedis pool singleton
│
├── dtos/
│   ├── LoginRequestDto.java        # Login request body
│   ├── LoginResponseDto.java       # Login response — accessToken + refreshToken
│   ├── RefreshResponseDto.java     # Refresh response — new refreshToken
│   └── RegisterRequestDto.java     # Registration request body
│
├── repositories/
│   ├── UserRepository.java         # MySQL — all user queries
│   └── RedisRepository.java        # Redis — get, set, delete, exists, ttl
│
├── records/
│   └── User.java                   # Domain record — maps from ResultSet
│
├── validators/
│   ├── FieldValidator.java         # Shared field validation — email, username, password
│   ├── LoginValidator.java         # Login-specific validation
│   ├── RegisterValidator.java      # Registration-specific validation
│   └── ValidationResult.java       # ok() / fail(errors) — throwIfInvalid()
│
├── handlers/
│   ├── AuthHandler.java            # POST /api/v1/auth/login
│   ├── LogoutHandler.java          # POST /api/v1/auth/logout
│   ├── RegisterHandler.java        # POST /api/v1/register
│   └── UserHandler.java            # GET  /api/v1/check-availability
│
├── services/
│   ├── AuthService.java            # login, logout, refresh
│   ├── RegisterService.java        # register — duplicate check, hash, save
│   ├── TokenService.java           # JWT + Redis token management
│   └── UserService.java            # checkAvailability
│
├── exceptions/
│   ├── DuplicateUserException.java
│   ├── FieldValidationException.java
│   ├── InexistentUserException.java
│   ├── UnauthorizedException.java
│   └── UnverifiedUserException.java
│
├── utilities/
│   ├── PasswordHasherUtil.java     # Argon2id hash + verify
│   ├── Logger.java                 # Structured logging
│   ├── ConfigReaderUtil.java       # Reads config values
│   ├── EnvReader.java              # Reads environment variables
│   └── PortUtil.java               # Checks if port is free before binding
│
├── enums/
│   ├── HttpMethod.java             # GET, POST, PUT, DELETE
│   ├── HttpStatus.java             # 200, 201, 400, 401, 403, 404, 409, 500
│   ├── DBProperty.java             # DB config keys
│   └── AppProperty.java            # App config keys
│
└── setup/
    ├── AppServer.java              # HTTP server — singleton, addContext, start, stop
    ├── ServerSetup.java            # Wires everything together — DI by hand
    └── Main.java                   # Entry point
```

---

## Database Setup

### Create the database

```sql
CREATE DATABASE auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auth_db;
```

### Create the users table

```sql
CREATE TABLE users (
    id                      BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username                VARCHAR(30)     NOT NULL UNIQUE,
    first_name              VARCHAR(50)     NOT NULL,
    last_name               VARCHAR(50)     NOT NULL,
    email                   VARCHAR(254)    NOT NULL UNIQUE,
    phone_number            VARCHAR(20)     NULL,
    password_hash           TEXT            NOT NULL,
    is_verified             BOOLEAN         NOT NULL DEFAULT FALSE,
    password_changed_at     TIMESTAMP       NOT NULL DEFAULT NOW(),
    date_of_birth           DATE            NULL,
    gender                  VARCHAR(20)     NULL,
    profile_picture_url     TEXT            NULL,
    country                 VARCHAR(100)    NULL,
    city                    VARCHAR(100)    NULL,
    street_address          VARCHAR(255)    NULL,
    postal_code             VARCHAR(20)     NULL,
    accepted_terms          BOOLEAN         NOT NULL DEFAULT FALSE,
    accepted_privacy_policy BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP       NULL DEFAULT NULL,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW() ON UPDATE NOW()
);
```

### Create indexes

```sql
CREATE INDEX idx_users_email       ON users(email);
CREATE INDEX idx_users_username    ON users(username);
```

---

## Environment Variables

Set these before running:

```bash
# Database
DB_URL=jdbc:mysql://localhost:3306/auth_db
DB_USERNAME=root
DB_PASSWORD=your_password

# Redis
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_USER=default
REDIS_PASSWORD=your_redis_password

# JWT
JWT_SECRET=your_secret_key_minimum_32_characters

# Server
PORT=8081
```

> For local development these can be hardcoded in `DBConnectionPool.java` and `RedisConnectionPool.java`. Never hardcode in production.

---

## Running the Server

```bash
# Compile
javac -cp "lib/*" -d out src/**/*.java

# Run
java -cp "out:lib/*" Main
```

Server starts at:
```
http://localhost:8081
```

---

## API Endpoints

### Register

```
POST /api/v1/register
Content-Type: application/json
```

**Request body:**

```json
{
    "username":              "johndoe",
    "firstName":             "John",
    "lastName":              "Doe",
    "email":                 "john.doe@gmail.com",
    "password":              "Secret@123!",
    "confirmPassword":       "Secret@123!",
    "acceptedTerms":         true,
    "acceptedPrivacyPolicy": true,

    "phoneNumber":           "+1234567890",
    "dateOfBirth":           "1990-05-15",
    "gender":                "male",
    "country":               "USA",
    "city":                  "New York",
    "streetAddress":         "123 Main St",
    "postalCode":            "10001",
    "profilePictureUrl":     null
}
```

Required: `username`, `email`, `password`, `confirmPassword`, `acceptedTerms`, `acceptedPrivacyPolicy`

Optional: `phoneNumber`, `dateOfBirth`, `gender`, `country`, `city`, `streetAddress`, `postalCode`, `profilePictureUrl`

**Responses:**

```json
// 201 — success
{ "message": "User registered successfully" }

// 400 — validation errors
{ "errors": ["Password must contain at least one uppercase letter"] }

// 409 — duplicate
{ "error": "Email or username already taken" }

// 500 — server error
{ "error": "An internal error occurred" }
```

---

### Login

```
POST /api/v1/auth/login
Content-Type: application/json
```

**Request body:**

```json
{
    "email":    "john.doe@gmail.com",
    "password": "Secret@123!"
}
```

**Responses:**

```json
// 200 — success
{
    "accessToken":  "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}

// 400 — validation errors
{ "errors": ["Email is required"] }

// 401 — wrong credentials
{ "error": "Invalid credentials" }

// 403 — email not verified
{ "error": "Please verify your email" }
```

---

### Logout

```
POST /api/v1/auth/logout
Authorization: Bearer <accessToken>
```

**Responses:**

```json
// 200 — success
{ "message": "Logged out successfully" }

// 401 — missing or invalid token
{ "error": "Missing or invalid Authorization header" }
```

---

### Check Availability

```
GET /api/v1/users/check-availability?email=john@gmail.com
GET /api/v1/users/check-availability?username=johndoe
GET /api/v1/users/check-availability?email=john@gmail.com&username=johndoe
```

**Responses:**

```json
// 200 — available
{ "available": true }

// 409 — taken
{ "available": false, "message": "Email already registered" }

// 400 — validation error
{ "errors": ["Email format is invalid"] }
```

---

## Password Rules

Enforced at registration by `FieldValidator.validatePasswordStrength()`:

- 8–128 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character (`!@#$%^&*` etc.)
- No repeated characters (`aaa`, `111`)
- No sequential characters (`abc`, `123`, `987`)
- No keyboard patterns (`qwerty`, `asdfgh`)
- No repeating groups (`abcabc`, `abab`)
- Minimum 4 distinct characters

Passwords are hashed with **Argon2id** before storage. Plain text passwords are never logged or stored anywhere.

---

## Token Design

| Token | Type | Storage | TTL |
|---|---|---|---|
| Access token | JWT | Client only | 15 minutes |
| Refresh token | Opaque UUID | Redis | 7 days |

**On logout:** access token is blacklisted in Redis until it naturally expires. Refresh token is deleted immediately.

**Redis key structure:**
```
refresh_token:{userId}   → refresh token value     TTL: 7 days
blacklist:{accessToken}  → "revoked"               TTL: remaining access token lifetime
```

---

## Security Highlights

- Argon2id password hashing — not BCrypt, not MD5
- JWT signed with HMAC256
- Refresh token rotation on each use
- Access token blacklisting on logout
- Attack detection on all input fields — SQL injection, XSS, path traversal
- Generic error messages — internal details never reach the client
- Passwords never logged anywhere in the codebase
- PreparedStatements throughout — no string concatenation in SQL

---

## Dependencies

```xml
<!-- HikariCP — connection pool -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>

<!-- MySQL driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>

<!-- Jackson — JSON parsing -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.0</version>
</dependency>

<!-- Argon2 — password hashing -->
<dependency>
    <groupId>de.mkammerer</groupId>
    <artifactId>argon2-jvm</artifactId>
    <version>2.11</version>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>java-jwt</artifactId>
    <version>4.4.0</version>
</dependency>

<!-- Jedis — Redis client -->
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>5.1.0</version>
</dependency>
```

---

## Notes

- `RETURNING *` is PostgreSQL syntax, not supported in MySQL. This project uses `Statement.RETURN_GENERATED_KEYS` + a follow-up `findById()` call after every insert.
- Email verification is not yet implemented. `is_verified` defaults to `FALSE`. To test login, manually verify a user: `UPDATE users SET is_verified = TRUE WHERE email = 'your@email.com';`
- No dependency injection framework — all wiring is done by hand in `ServerSetup.java`.
