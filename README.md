# AI-Powered Learning Management System (LMS)

> A modern, enterprise-grade AI-powered Learning Management System (LMS) backend built using **Java 21**, **Spring Boot 3.3.2**, **Spring AI (OpenAI)**, **PostgreSQL (PGVector)**, **Redis Cache**, and **Docker**.

---

## 🚀 Key Features

- **Course & Progress Management**: Full CRUD for Categories, Courses, Sections, and Lessons, with progress tracking (percentage completion).
- **Interactive Quizzes & Grading**: Auto-grading quizzes with multiple question types (Single/Multiple Choice, True/False, Short Answer).
- **Real-Time Notification Server**: Event-driven WebSockets (STOMP) server pushing immediate student enrollment updates, review changes, and assignments.
- **AI Tutor Chatbot**: Interactive Contextual SSE streaming chatbot utilizing course lessons and student histories.
- **AI Quiz Generation**: Generates complete, auto-graded quizzes directly from lesson text transcripts.
- **AI Course Recommendation**: Recommends personalized courses based on a student's history, with a cold-start popularity-based fallback.
- **AI Document RAG Pipeline**: Ingests, parses (PDF, DOCX, TXT), chunks, and indexes materials in PGVector to answer questions with unique source attributions.

---

## 🛠️ Technology Stack

- **Core**: Java 21, Spring Boot 3.3.2, Maven 3.9+
- **Persistence**: PostgreSQL 16+ (with `vector` extension), Spring Data JPA, Hibernate 6
- **Caching**: Redis Cache (`spring-boot-starter-data-redis`) with polymorphic serialization
- **Messaging**: Spring WebSocket STOMP
- **AI Engine**: Spring AI (BOM `1.0.9`) with OpenAI integrations
- **Logging**: Logback structured rolling logs
- **API Doc**: SpringDoc OpenAPI (Swagger UI)
- **Containerization**: Docker Compose (PGVector, Redis, App)

---

## 📦 Run & Installation

### Option A: Local Development Run (H2 & Memory Vector Store)
For quick offline development, compile and execute the test suites without any running external database:
```bash
# Start Spring Boot using the local profile (uses in-memory H2 DB & SimpleVectorStore)
$env:SPRING_PROFILES_ACTIVE="local"
mvn spring-boot:run
```

### Option B: Docker Compose Deployment (PostgreSQL + Redis + App Stack)
Orchestrates the entire platform (PostgreSQL, Redis, and Spring application) on a dedicated bridge network:
1. Ensure your Docker Desktop is running.
2. Set your OpenAI key as a local environment variable.
3. Build and launch the container cluster:
```bash
# Set your OpenAI API key
$env:SPRING_AI_OPENAI_API_KEY="your-api-key"

# Build and start all services
docker compose up --build
```

---

## 📊 Endpoints & Testing

- **Swagger API Explorer**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Spring Actuator Health**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **Spring Actuator Info**: [http://localhost:8080/actuator/info](http://localhost:8080/actuator/info)
- **Spring Actuator Metrics**: [http://localhost:8080/actuator/metrics](http://localhost:8080/actuator/metrics)

---

## 📁 System Architecture Documents

- [PROJECT_CONSTITUTION.md](PROJECT_CONSTITUTION.md) — Architectural standards, database schemas, package guidelines.
- [PROJECT_ROADMAP.md](PROJECT_ROADMAP.md) — 20-sprint development roadmap details.
- [PROJECT_DECISIONS.md](PROJECT_DECISIONS.md) — Architecture Decision Records (20 ADRs).
- [PROJECT_PROGRESS.md](PROJECT_PROGRESS.md) — Sprint completion logs.
- [AUTHENTICATION_FLOW.md](AUTHENTICATION_FLOW.md) — Token authentication and authorization rules.
