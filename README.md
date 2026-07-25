# Internship & Job Application Tracker

A job-application tracker built as an OOP coursework project, modeling job-seeking as an object hierarchy instead of a flat database table.

> University of Sri Jayewardenepura — Java OOP Course Project

## Overview

Applications are represented through an abstract `Application` class extended by two concrete subclasses — `InternshipApplication` and `FullTimeApplication` — each with its own required fields and its own valid status progression. Status changes are enforced through polymorphism and encapsulation, not type-checking: calling code never asks "is this an internship or a full-time application?" — it just calls `nextValidStates()` and `transitionTo()`, and each object answers according to its own rules.

## Features

- Track internship and full-time applications, each with a company, job role, and a status lifecycle (Applied → Interview → Offer → Accepted/Rejected, with internships able to convert to a full-time offer).
- Attach real documents (resume, cover letter, etc.) to an application — actual file upload, stored by the backend and downloadable later, not just a pasted link.
- Maintain a Companies list with industry/location, searchable and filterable, with enforced case-insensitive unique names, and link applications to a company for future grouping/reporting.
- Search and filter applications by company/job role/status/type; search and filter companies by name/industry/location.
- Edit or delete both applications and companies after creating them.

## Tech Stack

| Layer    | Technology                   |
| -------- | ---------------------------- |
| Backend  | Java 17, Spring Boot         |
| Database | MongoDB Atlas, Spring Data MongoDB |
| Frontend | React 18, Vite                |
| API      | REST, JSON over `fetch()`    |

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

## Project Structure

```
.
├── tracker/    Spring Boot REST API backend (Java 17, MongoDB)
└── frontend/   React (Vite) single-page app
```

Each has its own README with full setup and deployment details:

- [`tracker/README.md`](tracker/README.md) — backend setup, running locally, and deployment
- [`frontend/README.md`](frontend/README.md) — frontend setup, running locally, and deployment

## How It Works

Two separate processes run side by side on your machine (or, in production, on two separate hosts):

```
┌─────────────────────┐        HTTP requests to          ┌──────────────────────┐        MongoDB driver          ┌──────────────────┐
│  Browser             │        http://localhost:8080     │  Backend              │        (TLS connection)        │  MongoDB Atlas    │
│  http://localhost:5173│  ───────────────────────────►   │  Spring Boot API      │  ───────────────────────────►  │  (cloud database)  │
│  (React app / Vite)  │  ◄───────────────────────────    │  (Java, port 8080)    │  ◄───────────────────────────  │                    │
└─────────────────────┘        JSON responses             └──────────────────────┘        query results            └──────────────────┘
```

1. You open the **frontend** in a browser (`localhost:5173` in dev). It's just static HTML/CSS/JS — React renders the UI, nothing else.
2. Every action (loading applications, creating one, transitioning status, etc.) makes a `fetch()` call from the browser straight to the **backend** API at `VITE_API_BASE_URL` (`localhost:8080` in dev) — see `frontend/src/api.js`.
3. The backend (Spring Boot) receives the request, runs validation/business logic (e.g. "is this status transition allowed?"), and talks to **MongoDB Atlas** (a cloud-hosted database, not something running on your machine) to read/write data.
4. The backend responds with JSON; the frontend re-renders with the new data.

The backend never serves any HTML pages for the app itself (only a simple `/` info page) — it's a pure JSON API. The frontend and backend are two independent deployable things, which is why they live in separate `frontend/` and `tracker/` folders and have separate `README.md`/`.env` files.

## Ports

| Component | Default port | URL (local dev) | Configurable via |
| --- | --- | --- | --- |
| Backend (Spring Boot) | `8080` | http://localhost:8080 | `server.port` in `tracker/src/main/resources/application.properties`, or a `PORT` env var on most hosting platforms |
| Frontend (`npm run dev`) | `5173` | http://localhost:5173 | `server.port` in `frontend/vite.config.js` |

These two ports (8080 and 5173) are **only relevant when running locally**. In production each component gets its own public URL from whatever host you deploy it to (e.g. `https://tracker-backend.onrender.com` and `https://your-app.vercel.app`) — see [Deployment](#deployment).

## Getting Started (from scratch)

### Prerequisites

- Java 17+
- Maven
- Node.js 18+ (for the frontend)
- A free [MongoDB Atlas](https://www.mongodb.com/cloud/atlas/register) account (this project connects to Atlas in the cloud — no local MongoDB install needed)

### 1. Set up MongoDB Atlas (one-time, shared by everyone on the team)

If a teammate already has a cluster set up and has added you as a project member, skip to step 2 — you just need them to invite you (Atlas → Project Settings → Access Manager) and give you a connection string.

Otherwise, to create one from scratch:

1. Go to https://cloud.mongodb.com and sign up (free tier is enough).
2. Create a new **Project**, then **Build a Database** → choose the free **M0** tier → pick any cloud region → **Create**.
3. Under **Security → Database Access**: click **Add New Database User**, set a username and a generated password, grant **Read and write to any database**. Save the password somewhere safe — Atlas won't show it again.
4. Under **Security → Network Access**: click **Add IP Address**. Add your current IP, or `0.0.0.0/0` ("allow from anywhere") for simplicity during development.
5. Under **Database → Connect** → **Drivers**: copy the connection string. It looks like:
   ```
   mongodb+srv://<username>:<password>@<cluster-host>/?retryWrites=true&w=majority
   ```
   Replace `<username>`/`<password>` with the database user from step 3, and **add a database name** before the `?` — Atlas's template omits it:
   ```
   mongodb+srv://<username>:<password>@<cluster-host>/internship_tracker?retryWrites=true&w=majority
   ```

### 2. Backend

```bash
cd tracker
cp .env.example .env
```
Open `tracker/.env` and fill it in:
```
MONGODB_URI=mongodb+srv://<username>:<password>@<cluster-host>/internship_tracker?retryWrites=true&w=majority
CORS_ALLOWED_ORIGINS=http://localhost:5173
```
Then:
```bash
mvn test                # optional — runs the domain unit tests first
mvn spring-boot:run
```
Backend now runs on **http://localhost:8080**. Visit it in a browser — you should see an info page listing all endpoints (not a 404). Full details in [`tracker/README.md`](tracker/README.md).

### 3. Frontend

In a second terminal:
```bash
cd frontend
cp .env.example .env
```
Open `frontend/.env` — the default is already correct for local dev:
```
VITE_API_BASE_URL=http://localhost:8080
```
Then:
```bash
npm install
npm run dev
```
Frontend now runs on **http://localhost:5173**. Open that URL — you should see the app UI, and it should be able to list/create companies and applications (proving it can reach the backend). Full details in [`frontend/README.md`](frontend/README.md).

Both servers must be running at the same time for the app to work end-to-end.

## API

| Method | Endpoint                               | Description                                        |
| ------ | -------------------------------------- | -------------------------------------------------- |
| POST   | `/api/applications`                  | Create a new application (internship or full-time) |
| GET    | `/api/applications`                  | List all applications                              |
| GET    | `/api/applications/{id}`             | Get one application                                |
| PUT    | `/api/applications/{id}`             | Edit an application's fields (status is changed only via transition) |
| GET    | `/api/applications/{id}/next-states` | Get valid next statuses for this application       |
| PATCH  | `/api/applications/{id}/transition`  | Move to a new status                               |
| POST   | `/api/applications/{id}/documents/upload` | Upload a real file (resume, cover letter, etc.) — used by the frontend |
| POST   | `/api/applications/{id}/documents`   | Attach a document by URL (JSON, no file upload) — kept for API/Postman use |
| DELETE | `/api/applications/{id}`             | Delete an application                              |
| POST   | `/api/companies`                     | Create a company (rejects case-insensitive duplicate names) |
| GET    | `/api/companies`                     | List all companies                                  |
| GET    | `/api/companies/{id}`                | Get one company                                     |
| PUT    | `/api/companies/{id}`                | Edit a company (same duplicate-name check)          |
| DELETE | `/api/companies/{id}`                | Delete a company                                     |

## Data Model Note: Company vs. `companyName`, and Job Role

Every `Application` stores its own `companyName` (plain text), a required `jobRole` (plain text, e.g. "Backend Intern"), **and** an optional `companyId` linking to a separate `Company` record. They serve different purposes:

- `companyName` — always required, free text, lets you log an application immediately without first creating a formal company.
- `jobRole` — always required, free text. This is what lets you have multiple applications to the *same* company for *different* roles — there's no restriction on repeating a `companyName` across applications, only `jobRole` (combined with whatever else differs) tells them apart.
- `companyId` (optional) — links the application to a `Company` document (with industry/location) in the `companies` collection, so multiple applications to the same employer can be grouped/reported on later.

`Company` names must be unique, **case-insensitively** ("wso2" and "WSO2" count as the same name and the second create/rename is rejected) — this keeps the Companies list from splintering into near-duplicates. This uniqueness rule applies only to `Company` records, not to `Application.companyName`.

In the frontend's "New Application" form, picking a company from the dropdown auto-fills the name field — you can still type a name manually for a company that isn't in your list yet.

## Deployment

The backend and frontend deploy independently:

- **Backend** → any Java host (Render, Railway, Fly.io, a VPS). Builds to a runnable jar. See [`tracker/README.md`](tracker/README.md#deployment).
- **Frontend** → any static host (Vercel, Netlify, GitHub Pages). Builds to static files. See [`frontend/README.md`](frontend/README.md#deployment).

Whichever hosts you pick:

1. Point the frontend's `VITE_API_BASE_URL` at the deployed backend's public URL.
2. Add the frontend's deployed URL to the backend's `CORS_ALLOWED_ORIGINS` env var.
3. Add your backend's outbound IP (or `0.0.0.0/0` for simplicity) to MongoDB Atlas's Network Access list.

## Security Notes

- Never commit real database credentials. `tracker/.env` and `frontend/.env` are gitignored — only the `.env.example` templates (placeholder values) are committed.
- If a real secret was ever committed to this repo's history, rotate it in MongoDB Atlas rather than relying on removing it from a file.

## License

Coursework project — for academic use.
