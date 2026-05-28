# Development Guide

This guide captures the working conventions for ReNova.

## Language Standard

Project code, API messages, seed data, documentation, comments, commit messages, and tests should use clear English unless a file is specifically part of the Chinese i18n bundle.

## Commit Standard

Use concise professional English commit messages. Prefer focused commits that represent one stable logical change.

Examples:

```text
feat: add listing offer workflow
fix: normalize API validation errors
test: cover listing creation API
docs: align API reference with ReNova routes
```

## Local Configuration

Keep machine-specific settings out of version control. Use environment variables or ignored local configuration files for database credentials, JWT secrets, and deployment values.

Use these templates as the source of truth:

- `backend/.env.example`
- `frontend/.env.example`

The default database name and package namespace still use `novacart` for compatibility with the existing project history. Do not rename them casually; package renames are a separate migration.

The `test` profile is available from main resources so developers can run the API against H2 without a local MySQL server.

## Setup Checklist

1. Install Java 21, Node.js 20 or newer, npm, and MySQL 8 or compatible.
2. Create the local database and user from the README.
3. Set backend environment variables.
4. Run `.\mvnw.cmd test` from `backend/` on Windows, or `./mvnw test` on Unix-like shells.
5. Run the backend with `.\mvnw.cmd spring-boot:run`.
6. Set `VITE_API_BASE_URL` for the frontend if the backend is not available through the Vite `/api` proxy.
7. Run `npm install` from `frontend/`.
8. Run `npm run dev` from `frontend/`.

For a backend smoke run without MySQL, use:

```powershell
.\mvnw.cmd -Dspring-boot.run.profiles=test spring-boot:run
```

## Quality Expectations

- Keep controllers thin and delegate behavior to services.
- Use DTOs at API boundaries.
- Validate incoming requests.
- Return consistent JSON errors.
- Do not add browser-only mock fallbacks for real marketplace workflows.
- Add tests for business rules, API contracts, and frontend API wrappers as features are implemented.
- Run frontend unit tests, frontend build, and backend tests before pushing stable work.
