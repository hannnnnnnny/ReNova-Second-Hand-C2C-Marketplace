# Development Guide

This guide captures the working conventions for NovaCart Ecommerce.

## Language Standard

All project content must be English-only. This includes UI labels, validation messages, API messages, seed data, documentation, comments, commit messages, and test data.

## Commit Standard

Use concise professional English commit messages. Prefer focused commits that represent one stable logical change.

Examples:

```text
chore: initialize project documentation and repository structure
chore: scaffold Spring Boot backend
feat: add product and category domain models
fix: improve validation error responses
```

## Local Configuration

Keep machine-specific settings out of version control. Use environment variables or ignored local configuration files for database credentials, JWT secrets, and other sensitive values.

Use the backend and frontend environment templates as the source of truth:

- `backend/.env.example`
- `frontend/.env.example`

The backend defaults are suitable for local development when MySQL is running with the documented database and user. Production deployments must provide their own credentials and JWT secret.

## Setup Checklist

1. Install Java 21, Node.js 20 or newer, npm, and MySQL 8 or compatible.
2. Create the `novacart` database and local user.
3. Set backend environment variables.
4. Run `./mvnw test` from `backend/`.
5. Run `./mvnw spring-boot:run` from `backend/`.
6. Set `VITE_API_BASE_URL` for the frontend if the backend is not running on `http://localhost:8080/api`.
7. Run `npm install` from `frontend/`.
8. Run `npm run dev` from `frontend/`.

Windows PowerShell users can use `mvnw.cmd` and `npm.cmd` if shell policy blocks the default shims.

## Quality Expectations

- Keep controllers thin and delegate behavior to services.
- Use DTOs at API boundaries.
- Validate incoming requests.
- Return consistent JSON errors.
- Add tests for business rules and API behavior as features are implemented.
- Run available checks before committing a stable phase.
