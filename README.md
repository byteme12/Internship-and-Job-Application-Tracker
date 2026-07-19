# Internship & Job Application Tracker

A job-application tracker built as an OOP coursework project — modeling job-seeking as an object hierarchy instead of a flat database table.

> University of Sri Jayewardenepura — Java OOP Course Project

## Overview

Applications are represented through an abstract `Application` class extended by two concrete subclasses — `InternshipApplication` and `FullTimeApplication` — each with its own required fields and its own valid status progression. Status changes are enforced through polymorphism and encapsulation, not type-checking: calling code never asks "is this an internship or a full-time application?" — it just calls `nextValidStates()` and `transitionTo()`, and each object answers according to its own rules.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot |
| Database | MongoDB, Spring Data MongoDB |
| Frontend | React |
| API | REST, JSON over `fetch()` |

## Architecture

```
domain/       Application (abstract), InternshipApplication, FullTimeApplication, ApplicationStatus
exception/    InvalidTransitionException, ApplicationNotFoundException, GlobalExceptionHandler
repository/   TrackerRepository (interface) + ApplicationRepository (Spring Data MongoDB impl)
service/      ApplicationService — depends only on TrackerRepository, not on Mongo directly
controller/   ApplicationController — REST API
```

**Key design decisions:**

- **Abstraction** — `Application` is never instantiated directly; it exists purely as a contract both subclasses must fulfill.
- **Polymorphism** — `nextValidStates()` is overridden per subclass. An `InternshipApplication` can reach `CONVERTED_TO_FULLTIME`; a `FullTimeApplication` never can. No `instanceof` checks anywhere in the codebase.
- **Encapsulation** — `status` has no public setter. The only way to change it is `transitionTo()`, which checks the move against `nextValidStates()` and throws `InvalidTransitionException` if it's illegal.
- **Persistence abstraction** — the service layer depends on `TrackerRepository` (a plain interface), not on `ApplicationRepository` or MongoDB directly. The persistence technology can be swapped without touching business logic.
- **Polymorphic JSON** — Jackson's `@JsonTypeInfo`/`@JsonSubTypes` route incoming REST payloads to the correct subclass based on an `applicationType` field, with zero manual type-checking in the controller.

## Status Flow

**Internship:** `APPLIED → INTERVIEW → OFFER → ACCEPTED → (optionally) CONVERTED_TO_FULLTIME`
**Full-Time:** `APPLIED → INTERVIEW → OFFER → ACCEPTED`

`REJECTED` is reachable from `APPLIED`, `INTERVIEW`, or `OFFER` in both types. Conversion is never guaranteed — `ACCEPTED` is a valid terminal state on its own for internships.

## Getting Started

### Prerequisites
- Java 17
- Maven
- MongoDB (local or Docker)
- Node.js (for the frontend)

### Backend

```bash
# start MongoDB
docker run -d -p 27017:27017 --name tracker-mongo mongo

cd backend
mvn test           # run domain-level unit tests first
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm start
```

## API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/applications` | Create a new application (internship or full-time) |
| GET | `/api/applications` | List all applications |
| GET | `/api/applications/{id}` | Get one application |
| GET | `/api/applications/{id}/next-states` | Get valid next statuses for this application |
| PATCH | `/api/applications/{id}/transition` | Move to a new status |
| DELETE | `/api/applications/{id}` | Delete an application |

## Team

| Member | Responsibility |
|---|---|
| [Name] | Domain logic, exceptions, unit tests |
| [Name] | Persistence, REST API, frontend integration |

## License

Coursework project — for academic use.
