# Tracker Frontend

React (Vite) single-page app for the Internship and Job Application Tracker. See the [repo root README](../README.md) for the overall project.

## Features

- **Companies tab** — list, search (by name), and filter (by industry/location) companies; create, edit, and delete them. Duplicate names are rejected case-insensitively (matches the backend's rule — see [`../tracker/README.md`](../tracker/README.md#company-name-uniqueness-case-insensitive)).
- **Applications tab** — list, search (by company or job role), and filter (by job role/status/type) applications; create an internship or full-time application (with company, job role, and type-specific fields), view valid next states, transition status, upload a real document, edit any field, or delete.

All API calls live in `src/api.js`, matching the backend's REST endpoints described in [`../tracker/README.md`](../tracker/README.md#api).

**The "Company Name" field**, in the New/Edit Application form, is a single autocomplete input (backed by an HTML `<datalist>`): start typing and existing companies show up as suggestions, matched case-insensitively (typing "wso2" matches a company named "WSO2"). Pick one and this application links to that `Company` record (`companyId`) automatically. Keep typing a name that isn't suggested and it's saved as plain text only — no formal `Company` record required. See the [root README's Data Model Note](../README.md#data-model-note-company-vs-companyname-and-job-role) for why both exist.

**"Job Role"** is a separate required field on every application (e.g. "Backend Engineering Intern") — this is what lets you apply to the same company more than once for different roles without conflict; there's no uniqueness restriction on applications, only on `Company` records.

**Document uploads** use a real `<input type="file">` — the file is sent to the backend (which stores it and returns a URL), not typed in as a link. Backend enforces a 10MB per-file limit (see [`../tracker/README.md`](../tracker/README.md#file-uploads)). Uploaded documents appear as clickable download links on the application card.

**Editing**: both application cards and company cards have an "Edit" button that opens an inline form pre-filled with the current values (application type can't be changed on edit — delete and recreate if you need to switch between internship/full-time). "Delete" on either asks for confirmation first.

**Filter dropdowns are case-insensitive**: if your data has "Colombo" on one company and "colombo" on another, the Location filter shows one option (not two) and selecting it matches both.

## Prerequisites

- Node.js 18+
- The backend running (locally or deployed) — see [`../tracker/README.md`](../tracker/README.md). The frontend is useless on its own; it just calls the backend's REST API.

## Ports

| Component | Port | URL |
| --- | --- | --- |
| Frontend (`npm run dev`) | `5173` | http://localhost:5173 |
| Backend | `8080` | http://localhost:8080 |

Change the dev port in `vite.config.js` (`server.port`) if `5173` is already taken on your machine. If you do, also update the backend's `CORS_ALLOWED_ORIGINS` to match, or requests will be blocked by CORS.

## How It Works

This app is a pure client — it renders in the browser and calls the backend's REST API for every piece of data (nothing is stored locally). See the [root README's "How It Works"](../README.md#how-it-works) section for the full request flow diagram. In short: browser → this app (port 5173 in dev) → `fetch()` → backend API (port 8080 in dev) → MongoDB Atlas.

## Local Setup (from scratch)

The backend must already be set up and reachable (see [`../tracker/README.md`](../tracker/README.md) — get that running first, since this app has nothing to show without it).

1. Copy the env template:
   ```bash
   cp .env.example .env
   ```
2. Open `.env` — for local development against a locally-running backend, the default is already correct:
   ```
   VITE_API_BASE_URL=http://localhost:8080
   ```
   Only change this if your backend runs on a different port, or once you point this frontend at a deployed backend URL (see [Deployment](#deployment)).
3. Install dependencies and run:
   ```bash
   npm install
   npm run dev
   ```
4. Open `http://localhost:5173`. You should see the app UI load a (possibly empty) list of applications — if you instead see a network error in the browser console, double check the backend is running and its `CORS_ALLOWED_ORIGINS` (in `tracker/.env`) includes `http://localhost:5173`.

`.env` is gitignored — only `.env.example` (a placeholder template) is committed. Each person running this project locally creates their own `frontend/.env`.

## Project Structure

```
src/
├── api.js                    Thin fetch wrapper for every backend endpoint
├── App.jsx                   Tab layout (Applications / Companies)
├── main.jsx                  React entry point
├── index.css                 App-wide styling
└── components/
    ├── ApplicationsTab.jsx    Lists + searches/filters applications, hosts the create form
    ├── ApplicationCard.jsx    One application: transitions, add document, edit, delete
    ├── ApplicationForm.jsx    Shared form (internship/full-time fields toggle) — used by both create and edit
    ├── NewApplicationForm.jsx Thin wrapper: ApplicationForm configured for creating
    ├── CompaniesTab.jsx       Lists + searches/filters companies, hosts the create form
    ├── CompanyForm.jsx        Shared form — used by both create and edit
    └── NewCompanyForm.jsx     Thin wrapper: CompanyForm configured for creating
```

## Build

```bash
npm run build
```
Outputs static files to `dist/`, ready to deploy to a static host.

## Deployment

The frontend is a static site — deploy `dist/` to any static host. Example using [Vercel](https://vercel.com):

1. Push this repo to GitHub if it isn't already.
2. In Vercel: **New Project** → import the repo.
3. Configure:
   - **Root Directory**: `frontend`
   - **Framework Preset**: Vite (auto-detected)
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
   - **Environment variable**: `VITE_API_BASE_URL` = your deployed backend's URL (e.g. `https://tracker-backend.onrender.com`)
4. Deploy. Vercel gives you a public URL like `https://your-app.vercel.app`.
5. Go back to your backend's environment variables and set `CORS_ALLOWED_ORIGINS` to include this exact URL, then redeploy the backend.

The same steps work on Netlify (Base directory `frontend`, Build command `npm run build`, Publish directory `frontend/dist`) or GitHub Pages (with an extra `base` setting in `vite.config.js` matching your repo name).

### General deployment checklist (any static host)

- [ ] `VITE_API_BASE_URL` set as a build-time environment variable pointing at the deployed backend
- [ ] Backend's `CORS_ALLOWED_ORIGINS` includes this frontend's exact deployed URL (including `https://`, no trailing slash)
- [ ] Build with `npm run build`, serve the `dist/` folder
