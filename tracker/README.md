# Internship & Job Application Tracker — Backend

Java 17 + Spring Boot 3.2 + Spring Data JPA + MySQL.

## Project structure

```
domain/       Application (abstract), InternshipApplication, FullTimeApplication, ApplicationStatus
exception/    InvalidTransitionException, ApplicationNotFoundException, GlobalExceptionHandler
repository/   TrackerRepository (interface) + ApplicationRepository (Spring Data JPA impl)
service/      ApplicationService (business logic, depends only on TrackerRepository)
controller/   ApplicationController (REST API)
```

## Setup

1. Install Java 17 and Maven.
2. Create the database:
   ```sql
   CREATE DATABASE tracker_db;
   ```
3. Edit `src/main/resources/application.properties` — set your real MySQL
   username/password.
4. Run:
   ```
   mvn spring-boot:run
   ```
   Hibernate will auto-create the `applications` table (one table, both
   subclasses, discriminated by `application_type` column).

## API

| Method | Path | Body | Purpose |
|---|---|---|---|
| POST | `/api/applications` | `{ "applicationType": "INTERNSHIP", "companyName": "...", "dateApplied": "2026-07-01", "durationMonths": 3, "stipend": 500, "university": "USJ" }` | create |
| POST | `/api/applications` | `{ "applicationType": "FULLTIME", "companyName": "...", "dateApplied": "2026-07-01", "salary": 120000, "noticePeriodDays": 30 }` | create |
| GET | `/api/applications` | — | list all |
| GET | `/api/applications/{id}` | — | get one |
| GET | `/api/applications/{id}/next-states` | — | valid next statuses for this specific application |
| PATCH | `/api/applications/{id}/transition` | `{ "status": "INTERVIEW" }` | move to next status (409 if illegal) |
| DELETE | `/api/applications/{id}` | — | delete |

`applicationType` is what Jackson reads to decide which subclass to
deserialize the JSON into — this is the "polymorphic JSON deserialization"
piece from your project brief.

## Before touching the frontend

Test every endpoint above in Postman first. In particular:
- POST an internship, then PATCH it through APPLIED → INTERVIEW → OFFER →
  ACCEPTED → CONVERTED_TO_FULLTIME and confirm each step succeeds.
- POST a full-time application, PATCH it to ACCEPTED, then try PATCHing to
  CONVERTED_TO_FULLTIME — confirm you get a 409, not a 200.
- Try skipping a stage (APPLIED straight to OFFER) — confirm 409.

If all of that behaves correctly, the backend is done and the frontend is
just wiring `fetch()` calls to these exact endpoints.

## Tests

`mvn test` runs `ApplicationTransitionTest` — pure domain-level tests, no
Spring context or database needed, proving the state machine logic is
correct in isolation.
