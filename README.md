# AI-Powered LMS

> AI-Powered Learning Management System built with Spring Boot 3.x, Java 21, PostgreSQL.

## Tech Stack

- Java 21, Spring Boot 3.3.2, Maven
- PostgreSQL, Spring Data JPA, Hibernate 6
- MapStruct, Lombok, Jakarta Validation
- SpringDoc OpenAPI (Swagger UI)

## Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 16+

## Database Setup

```sql
CREATE USER lms_user WITH PASSWORD 'lms_password_123';
CREATE DATABASE lms_db OWNER lms_user;
GRANT ALL PRIVILEGES ON DATABASE lms_db TO lms_user;
```

## Run

```bash
mvn spring-boot:run
```

## API Documentation

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Project Documentation

- [PROJECT_CONSTITUTION.md](PROJECT_CONSTITUTION.md) — Architecture, coding standards, conventions
- [PROJECT_ROADMAP.md](PROJECT_ROADMAP.md) — Sprint-by-sprint development plan
- [PROJECT_DECISIONS.md](PROJECT_DECISIONS.md) — Architecture Decision Records
- [PROJECT_PROGRESS.md](PROJECT_PROGRESS.md) — Development progress tracking
