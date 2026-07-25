# Internship & Job Application Tracker — Backend

Java 17 + Spring Boot 3.2 + Spring Data MongoDB. See the [repo root README](../README.md) for the overall project and architecture rationale.

## Project structure

```
config/       CorsConfig — allows the frontend origin to call this API
domain/       Application (abstract), InternshipApplication, FullTimeApplication, ApplicationStatus, Company, ApplicationDocument
exception/    InvalidTransitionException, ApplicationNotFoundException, CompanyNotFoundException, GlobalExceptionHandler
repository/   TrackerRepository (interface) + ApplicationRepository (Spring Data MongoDB impl), CompanyRepository
service/      ApplicationService, CompanyService — depend only on the repository interfaces
controller/   ApplicationController, CompanyController, HomeController (REST API)
```

## Prerequisites

- Java 17+
- Maven (no wrapper is checked in — use your system `mvn`)
- A MongoDB Atlas cluster and connection string (Database Access user + your IP added to Network Access)

## Port

Runs on **`http://localhost:8080`** by default (Spring Boot's default port, unset in `application.properties`). To change it locally, add `server.port=<port>` to `application.properties`. On most hosting platforms (Render, Railway, etc.) the platform assigns a port via a `PORT` environment variable — see [Deployment](#deployment) below for wiring that up.

## Setup (from scratch)

### 1. Get a MongoDB Atlas connection string

Skip to step 2 if you already have one (e.g. a teammate shared `MONGODB_URI` with you).

1. Sign up free at https://cloud.mongodb.com.
2. **Build a Database** → free **M0** tier → any region → **Create**.
3. **Security → Database Access** → **Add New Database User** — set a username + generated password, role **Read and write to any database**. Copy the password now; Atlas won't show it again.
4. **Security → Network Access** → **Add IP Address** — add your current IP (or `0.0.0.0/0` for simplicity in dev).
5. **Database → Connect → Drivers** → copy the connection string template:
   ```
   mongodb+srv://<username>:<password>@<cluster-host>/?retryWrites=true&w=majority
   ```
   Fill in your real username/password, and **insert a database name** before the `?` (Atlas's template omits it — without it, the app fails at startup with `Database name must not be empty`):
   ```
   mongodb+srv://<username>:<password>@<cluster-host>/internship_tracker?retryWrites=true&w=majority
   ```

### 2. Create your local `.env` file

This repo never commits real credentials — you create your own local `tracker/.env` from the template:
```bash
cp .env.example .env
```
Open `.env` and fill it in:
```
MONGODB_URI=mongodb+srv://<username>:<password>@<cluster-host>/internship_tracker?retryWrites=true&w=majority
CORS_ALLOWED_ORIGINS=http://localhost:5173
```
- `MONGODB_URI` — from step 1 above.
- `CORS_ALLOWED_ORIGINS` — comma-separated list of frontend origins allowed to call this API. `http://localhost:5173` is the frontend's local dev address (see `frontend/README.md`); add your deployed frontend URL here too once you deploy it.

`.env` is gitignored (see root `.gitignore`) — it stays local to your machine. Only `.env.example` (placeholders, no real secrets) is committed.

### 3. Run it

```bash
mvn spring-boot:run
```
No manual table/collection creation needed — Spring Data MongoDB creates collections (`applications`, `companies`) on first write. Both `Application` subclasses share the `applications` collection, discriminated by an `applicationType` field. Once running, open `http://localhost:8080` in a browser — you should see an info page listing endpoints, confirming the server and its config are working.

## API

| Method | Path                                    | Body                                                                                                                                                    | Purpose                                            |
| ------ | ---------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------- |
| POST   | `/api/applications`                    | `{ "applicationType": "INTERNSHIP", "companyName": "...", "jobRole": "...", "dateApplied": "2026-07-01", "durationMonths": 3, "stipend": 500, "university": "USJ" }`   | create an internship application                    |
| POST   | `/api/applications`                    | `{ "applicationType": "FULLTIME", "companyName": "...", "jobRole": "...", "dateApplied": "2026-07-01", "salary": 120000, "noticePeriodDays": 30 }`                     | create a full-time application                      |
| GET    | `/api/applications`                    | —                                                                                                                                                        | list all                                             |
| GET    | `/api/applications/{id}`               | —                                                                                                                                                        | get one                                              |
| PUT    | `/api/applications/{id}`               | same shape as create (must keep the same `applicationType`)                                                                                             | edit an application's fields (not status — use transition below) |
| GET    | `/api/applications/{id}/next-states`   | —                                                                                                                                                        | valid next statuses for this specific application    |
| PATCH  | `/api/applications/{id}/transition`    | `{ "status": "INTERVIEW" }`                                                                                                                             | move to next status (409 if illegal)                 |
| POST   | `/api/applications/{id}/documents/upload` | multipart/form-data: `file` (the file), `documentType` (e.g. `RESUME`)                                                                             | upload and attach a real file — what the frontend uses |
| POST   | `/api/applications/{id}/documents`     | `{ "documentType": "RESUME", "fileName": "resume.pdf", "fileUrl": "https://..." }`                                                                    | attach a document by URL, no upload (JSON, Postman-friendly) |
| DELETE | `/api/applications/{id}`               | —                                                                                                                                                        | delete                                                |
| POST   | `/api/companies`                       | `{ "name": "...", "industry": "...", "location": "..." }`                                                                                              | create a company (400 if the name already exists, case-insensitively) |
| GET    | `/api/companies`                       | —                                                                                                                                                        | list all companies                                    |
| GET    | `/api/companies/{id}`                  | —                                                                                                                                                        | get one company                                       |
| PUT    | `/api/companies/{id}`                  | `{ "name": "...", "industry": "...", "location": "..." }`                                                                                              | edit a company (same case-insensitive name-uniqueness check, excluding itself) |
| DELETE | `/api/companies/{id}`                  | —                                                                                                                                                        | delete a company                                       |
| GET    | `/`                                     | —                                                                                                                                                        | HTML page listing all endpoints (sanity check in a browser) |

`applicationType` is what Jackson reads to decide which subclass (`InternshipApplication` or `FullTimeApplication`) to deserialize the JSON into — polymorphic JSON deserialization via `@JsonTypeInfo`/`@JsonSubTypes` on `Application`.

### `companyName` vs. `companyId` / `Company`

Every `Application` has a required `companyName` (plain text) and an optional `companyId` pointing at a `Company` document in the separate `companies` collection.

- `companyName` exists so you can log an application without first creating a formal `Company` record — quick, no referential integrity required.
- `companyId` is an optional link to a `Company` (which holds `industry`/`location`) — useful once you want to group multiple applications under the same employer, or attach richer company data, without duplicating it on every application.

They're independent: you can set `companyName` alone, or both. The frontend's create/edit application form auto-fills `companyName` (matched case-insensitively) when you pick a company from the dropdown, but still lets you type a name manually if the company isn't in your list yet.

### Job Role

Every `Application` also has a required `jobRole` (e.g. "Backend Engineering Intern") — plain text, separate from `companyName`. This is what makes it possible to apply to the same company for multiple different roles: nothing stops you from creating two applications with the same `companyName` (or the same `companyId`) as long as `jobRole` differs. There's no uniqueness constraint on applications at all — only `Company` records (see below) are required to be unique.

### Company Name Uniqueness (case-insensitive)

`Company` records must have a unique `name`, checked **case-insensitively** — creating or renaming a company to "wso2" fails with a 400 if "WSO2" (or "Wso2", etc.) already exists (`CompanyRepository.findByNameIgnoreCase`, enforced in `CompanyService`). This keeps one company from being accidentally split into multiple near-duplicate records due to inconsistent capitalization. It does **not** restrict `Application.companyName` — that field is free text and applications to the same company (regardless of casing) are expected and unrestricted.

## File Uploads

`POST /api/applications/{id}/documents/upload` accepts a real file (`multipart/form-data`) and:

1. Saves it to disk under the directory set by `app.upload.dir` (env var `UPLOAD_DIR`, defaults to `uploads/` relative to wherever the app runs — i.e. `tracker/uploads/` when running via `mvn spring-boot:run` from `tracker/`), prefixed with a random UUID so two people uploading `resume.pdf` don't collide.
2. Serves it back statically at `/uploads/<stored-filename>` — e.g. `http://localhost:8080/uploads/3f2a...-resume.pdf` — via a resource handler in `config/FileStorageConfig.java`.
3. Stores that path as the document's `fileUrl` on the application, and the original filename as `fileName`.

Limits: 10MB per file (`spring.servlet.multipart.max-file-size` in `application.properties`) — increase there if you need to accept larger files.

`uploads/` is gitignored — uploaded files never get committed. **This also means they're local to whichever machine/host is running the backend.** On a host with an ephemeral filesystem (e.g. Render's free tier), uploaded files are lost on every redeploy or restart. That's fine for local development or a coursework demo; for anything that needs uploads to survive redeploys, swap this for a persistent object store (S3, Cloudinary, etc.) instead of local disk.

## Before touching the frontend

Test every endpoint above (via Postman, curl, or similar) first. In particular:

- POST an internship, then PATCH it through APPLIED → INTERVIEW → OFFER → ACCEPTED → CONVERTED_TO_FULLTIME and confirm each step succeeds.
- POST a full-time application, PATCH it to ACCEPTED, then try PATCHing to CONVERTED_TO_FULLTIME — confirm you get a 409, not a 200.
- Try skipping a stage (APPLIED straight to OFFER) — confirm 409.

If all of that behaves correctly, the backend is done and the frontend is just wiring `fetch()` calls to these exact endpoints (already done in `../frontend`).

## Tests

```bash
mvn test
```
Runs `ApplicationDomainTest` — pure domain-level tests, no Spring context or database needed, proving the state machine logic is correct in isolation.

## Troubleshooting

- **`SSLException: Received fatal alert: internal_error`** connecting to Atlas — your current IP isn't in Atlas's Network Access allowlist. Add it (or `0.0.0.0/0` for dev) in Atlas → Network Access.
- **`bad auth: authentication failed`** — the username/password in `MONGODB_URI` doesn't match an Atlas Database Access user. Check/reset it under Atlas → Database Access.
- **`Database name must not be empty`** — `MONGODB_URI` is missing the `/<database-name>` segment before the `?`.
- **Frontend requests fail with a CORS error in the browser console** — the frontend's origin isn't in `CORS_ALLOWED_ORIGINS`.

## Deployment

The backend builds to a self-contained executable jar and runs on any host with Java 17+. Example using [Render](https://render.com) (free-tier friendly), but the same jar works on Railway, Fly.io, a VPS, etc.:

1. Push this repo to GitHub if it isn't already.
2. In Render: **New → Web Service** → connect the repo.
3. Configure:
   - **Root Directory**: `tracker`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/tracker-0.0.1-SNAPSHOT.jar`
   - **Environment variables**: `MONGODB_URI` and `CORS_ALLOWED_ORIGINS` (set to your deployed frontend's URL, e.g. `https://your-app.vercel.app`) — set these in Render's dashboard, never commit them.
4. Deploy. Render gives you a public URL like `https://tracker-backend.onrender.com`.
5. In MongoDB Atlas → Network Access, add Render's outbound IP (or `0.0.0.0/0` if your plan doesn't expose a static IP — fine for a small project, not ideal for production).
6. Update the frontend's `VITE_API_BASE_URL` to this deployed URL (see [`../frontend/README.md`](../frontend/README.md#deployment)).

### General deployment checklist (any host)

- [ ] `MONGODB_URI` set as an environment variable on the host (never in a committed file)
- [ ] `CORS_ALLOWED_ORIGINS` set to your deployed frontend's exact URL
- [ ] Atlas Network Access allows the host's outbound IP
- [ ] Build with `mvn clean package`, run with `java -jar target/tracker-0.0.1-SNAPSHOT.jar`
- [ ] If your host requires binding to a specific port (Render, Railway do via a `PORT` env var), add `server.port=${PORT:8080}` to `application.properties`
