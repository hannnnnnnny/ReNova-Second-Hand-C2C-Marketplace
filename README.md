# NovaCart Ecommerce

NovaCart Ecommerce is a full-stack online store system for small merchants. The project is designed as a production-style application with a Spring Boot backend, a Vue 3 storefront and admin interface, MySQL persistence, JWT-based admin authentication, and a RESTful JSON API.

The application is being built in focused phases so each stable milestone can be reviewed, tested, committed, and shipped independently.

## Tech Stack

- Backend: Java, Spring Boot, Maven
- Frontend: Vue 3, Vite, Vue Router, Pinia
- Database: MySQL
- ORM: Spring Data JPA and Hibernate
- Authentication: JWT with BCrypt password hashing
- API style: RESTful JSON
- Styling: Modern responsive CSS

## Repository Structure

```text
novacart-ecommerce/
  backend/
    src/main/java/com/novacart/store/
      config/
      controller/
      dto/
      entity/
      exception/
      repository/
      security/
      service/
      service/impl/
    src/main/resources/
    src/test/java/com/novacart/store/
  frontend/
    src/
      api/
      assets/
      components/
      layouts/
      pages/
      router/
      stores/
      utils/
  docs/
```

## Development Workflow

The project uses small, meaningful commits. Each commit should represent a stable unit of work and use a professional English commit message.

Core workflow:

1. Inspect the current repository state.
2. Implement one logical project stage.
3. Run the relevant checks available for that stage.
4. Review `git status`.
5. Commit the stable stage.
6. Push completed work to the remote branch.

## Backend Setup

The backend will live in `backend/` and will use Maven. Spring Boot configuration, MySQL connection settings, seed data, validation, security, and domain APIs will be added as the backend is implemented.

## Frontend Setup

The frontend will live in `frontend/` and will use Vite with Vue 3. Public storefront pages, admin pages, reusable components, API clients, routing, state management, and responsive styling will be added as the frontend is implemented.

## MySQL Setup

NovaCart is planned for MySQL. Local development should use a dedicated database and an application user with only the privileges required for this project.

Recommended local database name:

```text
novacart
```

## Environment Variables

Environment-specific values should be supplied through local environment variables or ignored local configuration files. Secrets must not be committed.

Planned backend variables:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION_MINUTES
```

## API Overview

The backend will expose public storefront APIs for categories, products, cart checkout, and order confirmation. Admin APIs will be protected with JWT authentication and will support product, category, order, dashboard, and inventory management.

## Default Admin Account

A seeded admin account will be added with the backend data initialization phase. The default credentials will be documented once the authentication and seed data are implemented.

## Development Notes

- All visible content, seed data, comments, API messages, documentation, and commit messages must be written in professional English.
- Public storefront APIs must remain accessible without authentication.
- Admin APIs must require a valid JWT.
- Controllers should delegate business rules to services.
- DTOs should be used for request and response boundaries.
- Validation and error responses should be consistent.

## Troubleshooting

- If Maven commands fail, confirm that a supported JDK is installed and available on the system path.
- If the backend cannot connect to MySQL, verify the database name, credentials, port, and environment variables.
- If frontend dependency installation fails, remove `node_modules`, verify the Node.js version, and install dependencies again.
- If admin requests fail with authentication errors, sign in again and confirm that the stored JWT has not expired.
