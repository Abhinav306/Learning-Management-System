# AI-Powered LMS — Project Progress

> This document tracks development progress. **Append only — never rewrite.**

---

## Current Status

| Field | Value |
|---|---|
| **Current Phase** | Phase 0 — Architecture & Planning |
| **Current Sprint** | Pre-Sprint |
| **Start Date** | 2026-07-11 |
| **Last Updated** | 2026-07-11 |

---

## Sprint Log

### Phase 0 — Architecture & Planning

**Status**: ✅ Completed 
**Date**: 2026-07-11

#### Deliverables

| Document | Status | Purpose |
|---|---|---|
| `PROJECT_CONSTITUTION.md` | ✅ Done | Project rulebook — architecture, standards, conventions |
| `PROJECT_ROADMAP.md` | ✅ Done | Sprint-by-sprint development plan (20 sprints, ~126 days) |
| `PROJECT_DECISIONS.md` | ✅ Done | Architecture Decision Records (20 ADRs) |
| `PROJECT_PROGRESS.md` | ✅ Done | This file — progress tracking |

#### Database Status
- [ ] PostgreSQL `lms_db` — not yet created
- [ ] No tables — schema pending Sprint 1

#### API Status
- [ ] No endpoints implemented
- [ ] Planned base: `/api/v1/`

#### Known Issues
- None

#### Notes
- All development must follow `PROJECT_CONSTITUTION.md`
- All architectural decisions are recorded in `PROJECT_DECISIONS.md`
- Sprint execution follows `PROJECT_ROADMAP.md`

---

*Next: Sprint 1 — Project Foundation*

---

### Sprint 1: Project Foundation
**Status**: ✅ Completed
**Date**: 2026-07-11

- Setup Maven dependency skeleton and multi-profile setups.
- Implemented BaseEntity mapping automatic audit logs.
- Configured structured ApiResponse wraps and exceptions handlers.

---

### Sprint 2: User Module
**Status**: ✅ Completed
**Date**: 2026-07-11

- Implemented User module CRUD (UserRole: STUDENT, INSTRUCTOR, ADMIN).
- Configured MapStruct mapping validations.

---

### Sprint 3: Authentication & Authorization
**Status**: ✅ Completed
**Date**: 2026-07-11

- Implemented JWT token provider and security filters.
- Setup RTR token rotation database bindings.
- Bootstrapped admin user credentials and protected UserController.

---

### Sprint 4: Course Management
**Status**: ✅ Completed
**Date**: 2026-07-11

- Configured Category self-referencing hierarchy slug indexing.
- Setup Course status and Section order mappings.
- Confirmed PreAuthorize checks blocking student modifications.

---

### Sprint 5: Lesson Management
**Status**: ✅ Completed
**Date**: 2026-07-11

- Created Lesson entity and media enum formats (VIDEO, TEXT, PDF, MIXED).
- Built transactional batch reorder mapping.
- Allowed public GET reads for courses/categories while securing write scopes.

*Next: Sprint 6 — Enrollment*

---

### Sprint 6: Enrollment
**Status**: ✅ Completed
**Date**: 2026-07-11

- Implemented Enrollment entity with unique constraint database bounds.
- Blocked self-enrollment by course instructors and secured course rosters.
- Implemented Dropout and Reactivation endpoints.

*Next: Sprint 7 — Progress Tracking*

---

### Sprint 7: Progress Tracking
**Status**: ✅ Completed
**Date**: 2026-07-11

- Implemented LessonProgress entity with unique constraint database bounds.
- Configured dynamic completion percentage calculations.
- Integrated auto-completion state shifts and ACTIVE reversion triggers.

*Next: Sprint 8 — Assignments*

---

### Sprint 8: Assignments
**Status**: ✅ Completed
**Date**: 2026-07-12

- Implemented Assignment and AssignmentSubmission entities with uniqueness constraints.
- Configured dynamic late submission flaggers checking due dates.
- Integrated instructor grading score bounds [0, maxScore] and feedback updates.

*Next: Sprint 9 — Quiz Module*

---

### Sprint 9: Quiz Module
**Status**: ✅ Completed
**Date**: 2026-07-12

- Implemented Quiz, QuizQuestion, QuizAttempt, and QuizAnswer entities.
- Secured student attempt responses from leaking correct answers or explanations.
- Configured dynamic case-insensitive auto-grading engine evaluating score percentages and attempt count limits.

*Next: Sprint 10 — Course Reviews*

---

### Sprint 10: Course Reviews
**Status**: ✅ Completed
**Date**: 2026-07-12

- Implemented Review entity with composite student/course uniqueness constraints.
- Configured dynamic rating averages query calculations.
- Integrated update and deletion rules, ensuring clean score resets when empty.

*Next: Sprint 11 — File Upload*

---

### Sprint 11: File Upload
**Status**: ✅ Completed
**Date**: 2026-07-12

- Created StorageService and LocalStorageService with validation checks (size limits, file extensions, and path traversal guards).
- Configured dynamic absolute server URL resolves for uploaded resources.
- Exposed public token-free file GET handlers with dynamic MIME type headers.

*Next: Sprint 12 — Notifications & WebSocket*

---

### Sprint 12: Notifications & WebSocket
**Status**: ✅ Completed
**Date**: 2026-07-12

- Created Notification database entity and repository mapping recipient users.
- Configured WebSocket STOMP endpoint paths (`/ws`) and user-specific brokers (`/queue`).
- Wired real-time notifications on Student Course Enrollment, Assignment Grading, and Quiz Completions.
- Exposed REST history endpoints (fetch notification lists, check unread counts, mark read, and mark all read).

*Next: Sprint 13 — Redis Caching*

---

### Sprint 13: Redis Caching
**Status**: ✅ Completed
**Date**: 2026-07-12

- Added `spring-boot-starter-data-redis` dependency to Maven configuration.
- Configured custom Redis Cache Manager with Jackson serializers supporting java.time module and type mapping.
- Implemented fallback custom Cache Error Handler resolving to graceful database query execution if Redis connection fails.
- Integrated caching annotations across Category, Course, and User service modules.

*Next: Sprint 14 — AI Tutor*

---

### Sprint 14: AI Tutor
**Status**: ✅ Completed
**Date**: 2026-07-12

- Added Spring AI BOM and OpenAI model starter configurations.
- Implemented `AiChatSession` and `AiChatMessage` database entities and repositories.
- Integrated chat history replay and context-aware course tutoring prompts in `AiTutorServiceImpl`.
- Implemented SSE (Server-Sent Events) streaming controller endpoint `POST /api/v1/ai/tutor/sessions/{sessionId}/messages`.
- Added unit and integration tests covering the service layer and controller mapping routes.

*Next: Sprint 15 — AI Quiz Generation*


