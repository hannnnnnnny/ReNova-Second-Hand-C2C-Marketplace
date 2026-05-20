# NovaCart Deployment Notes

NovaCart is a portfolio fashion commerce system. These notes describe safe deployment preparation without exposing real secrets or implying that the demo checkout is a real payment flow.

## Current Free Preview

Temporary local tunnel preview:

[https://quiet-olives-hear.loca.lt](https://quiet-olives-hear.loca.lt)

This preview is free, but it is not permanent hosting. It works only while the local Vite server and LocalTunnel process are running on the developer machine. Use it for quick review, demos, and screenshots. Use Vercel, Netlify, Cloudflare Pages, Render, Railway, or a similar host for a stable portfolio URL.

Current local tunnel commands:

```powershell
cd frontend
npm run dev -- --host 127.0.0.1 --port 5174
npx localtunnel --port 5174 --local-host 127.0.0.1
```

## Production Checklist

- Replace the default admin password after the first deployment.
- Set a long random `JWT_SECRET` through the hosting provider secret manager.
- Use managed MySQL credentials supplied through environment variables.
- Set `CORS_ALLOWED_ORIGINS` to the deployed frontend origin.
- Set `VITE_API_BASE_URL` to the deployed backend API root before building the frontend.
- Configure HTTPS at the platform, load balancer, or reverse proxy layer.
- Add monitoring, backup, rate limiting, and log retention before real operational use.
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

The compose file uses local demo credentials only. Edit `.env` before starting Compose if you need different host ports, database credentials, frontend origin, or API URL. The backend container is built with a Maven build image and runs the packaged Spring Boot application on a slim Java runtime image.

Useful validation command:

```bash
docker compose config
```

Do not reuse local Docker credentials, JWT secrets, or exposed database ports in production.

## Backend Hosting

Render, Railway, Fly.io, and similar platforms can run the backend as a Java service or Docker container.

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

Frontend-only portfolio demo:

- Root directory: `frontend`
- Build command: `npm run build`
- Publish/output directory: `dist`
- Vercel config: `frontend/vercel.json`
- Netlify config: `frontend/netlify.toml`

The frontend-only deployment supports the generated storefront demos, cart, demo checkout, local order tracking, and most portfolio presentation paths. Admin API calls that require Spring Boot will show unavailable-server messaging until the backend is deployed.

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

## Payment Limitation

NovaCart checkout creates an order and deducts stock, but no payment provider is connected. A real deployment that accepts payments should add provider-specific checkout sessions, webhook verification, idempotency keys, payment status reconciliation, and secure customer-facing order access.
