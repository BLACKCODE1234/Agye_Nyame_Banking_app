# Agye Nyame Banking App

A desktop banking application with a **JavaFX** frontend and a **Spring Boot** backend.

## Modules

- `backend/` — Spring Boot REST API (Spring Web, Spring Data JPA, Spring Security, H2 dev DB, JWT, Argon2id PIN hashing, OTP).
- `frontend/` — JavaFX desktop client that talks to the backend over HTTP.

## Features

- **Signup** (firstName, lastName, email, mobileNumber, pin, confirmPin)
- **Login** (mobileNumber, pin) issuing a JWT
- **OTP** second factor (request + verify), dev-mode logs the OTP
- **Deposit** (amount)
- **Withdraw** (amount, pin)
- **Transfer** (recipient mobileNumber, amount, pin) — atomic debit/credit
- **Account history**

PINs are hashed with **Argon2id** (never stored or logged in plaintext).

## Database (PostgreSQL)

The backend uses **PostgreSQL**. Create a database and user, for example:

```sql
CREATE DATABASE bankdb;
CREATE USER bank WITH PASSWORD 'bank';
GRANT ALL PRIVILEGES ON DATABASE bankdb TO bank;
```

Or run one with Docker:

```bash
docker run --name bank-postgres -e POSTGRES_DB=bankdb \
  -e POSTGRES_USER=bank -e POSTGRES_PASSWORD=bank \
  -p 5432:5432 -d postgres:16
```

Connection is configured via env vars (with dev defaults):
`DB_URL` (default `jdbc:postgresql://localhost:5432/bankdb`), `DB_USERNAME` (`bank`), `DB_PASSWORD` (`bank`).

## Running the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Tables are auto-created via Hibernate `ddl-auto=update`.

## Running the frontend

```bash
cd frontend
mvn javafx:run
```

Set the backend base URL via the `BANK_API_BASE_URL` env var (defaults to `http://localhost:8080`).

## Security notes

- PINs hashed with Argon2id (`Argon2PasswordEncoder`, backed by Bouncy Castle).
- JWT-based stateful-less auth for protected endpoints.
- OTP required as a second factor for sensitive operations; in dev the OTP is logged instead of sent via SMS. Plug a real `SmsSender` implementation for production.
- No real secrets are committed; the JWT secret has a dev default and should be overridden via `JWT_SECRET` in production.
