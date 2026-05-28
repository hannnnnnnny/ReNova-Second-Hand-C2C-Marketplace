# ReNova Deployment Notes

ReNova is a portfolio marketplace with a Vue frontend and Spring Boot API. These notes separate the free static preview from a backend-connected deployment so the project does not imply unsupported production capacity.

## Current Free Preview

Stable GitHub Pages preview:

[https://hannnnnnnny.github.io/NovaCart-Fashion-Commerce-Platform/](https://hannnnnnnny.github.io/NovaCart-Fashion-Commerce-Platform/)

The repository path still contains the old project name, so the Pages URL keeps that path. GitHub Pages serves the static Vue build only; it does not run the Spring Boot API or MySQL database. Any backend-connected marketplace operation needs a deployed API and `VITE_API_BASE_URL`.

If GitHub shows a Pages setup prompt, open repository settings, go to **Pages**, set **Build and deployment > Source** to **Deploy from a branch**, then choose `gh-pages` and `/ (root)`.

## Production Checklist

- Set a long random `JWT_SECRET` through the hosting provider secret manager.
- Use managed MySQL credentials supplied through environment variables.
- Set `CORS_ALLOWED_ORIGINS` to the deployed frontend origin.
- Set `VITE_API_BASE_URL` to the deployed backend API root before building the frontend.
- Configure HTTPS at the platform, load balancer, or reverse proxy layer.
- Add monitoring, backups, rate limiting, and log retention before real operational use.
- Integrate a payment provider before accepting real paid orders.

## Docker Compose

The repository includes `docker-compose.yml` for local containerized preview:

```bash
cp docker.env.example .env
docker compose up --build
```

On Windows PowerShell:

```powershell
Copy-Item docker.env.example .env
docker compose up --build
```

Local container URLs:

- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080/api`
- MySQL: `localhost:3306`

The compose file uses local demo credentials only. Edit `.env` before starting Compose if you need different host ports, database credentials, frontend origin, or API URL.

## Backend Hosting

Render, Railway, Fly.io, Azure App Service, AWS ECS, and similar platforms can run the backend as a Java service or Docker container.

Required backend variables:

```text
DB_HOST=
DB_PORT=
DB_NAME=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION_MINUTES=120
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.example
SERVER_PORT=8080
```

Build command for a Java service:

```bash
cd backend && ./mvnw -DskipTests package
```

Start command:

```bash
java -jar backend/target/backend-0.0.1-SNAPSHOT.jar
```

## Frontend Hosting

Vercel, Netlify, Cloudflare Pages, and static hosting platforms can serve the Vite build.

Required frontend variable for a backend-connected deployment:

```text
VITE_API_BASE_URL=https://your-backend-domain.example/api
```

Build command:

```bash
cd frontend && npm ci && npm run build
```

Publish directory:

```text
frontend/dist
```

## Database Hosting

Use a managed MySQL-compatible database for deployed environments. Configure network access only for the backend service and avoid exposing MySQL directly to the public internet.

Recommended operational safeguards:

- Automated backups.
- Point-in-time recovery if available.
- Restricted database users.
- Separate databases or schemas for staging and production.
- Migration strategy before production schema changes.

## 100,000 Concurrent Form Clicks

The repository can prevent obvious application-level mistakes such as duplicate frontend submissions, invalid states, and unsafe API errors. True 100,000-concurrent-click stability requires paid or free-tier-limited infrastructure decisions:

- CDN for static assets.
- Load-balanced backend replicas.
- Database connection pooling and managed MySQL sizing.
- Rate limiting and queueing for bursty writes.
- Load tests that simulate realistic logged-in traffic.
- Observability for latency, errors, database pressure, and saturation.

Those services usually cost money once traffic is real. The exact cost depends on provider, region, traffic volume, database size, and uptime target.

## Payment Limitation

ReNova order payment currently records a marketplace state transition. A real deployment that accepts payments should add provider checkout sessions, webhook verification, idempotency keys, payment status reconciliation, refunds, and secure customer-facing order access.
