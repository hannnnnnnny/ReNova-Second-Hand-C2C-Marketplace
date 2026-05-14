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

## Quality Expectations

- Keep controllers thin and delegate behavior to services.
- Use DTOs at API boundaries.
- Validate incoming requests.
- Return consistent JSON errors.
- Add tests for business rules and API behavior as features are implemented.
- Run available checks before committing a stable phase.
