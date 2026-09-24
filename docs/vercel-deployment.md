# ReNova on Vercel (demo mode)

Vercel's free tier hosts the Vue frontend only; the Spring Boot + MySQL backend is not deployed there. The Vercel build runs in demo mode (`VITE_RENOVA_DEMO=true`), where an in-browser adapter serves seeded listings, accounts, messages and orders. Nothing is persisted on a server, and payments and shipping remain simulated.

## Project settings

- Root directory: `frontend`
- Framework preset: Vite (from `frontend/vercel.json`)
- Environment variable (Production and Preview): `VITE_RENOVA_DEMO=true`

`vercel.json` rewrites every path to `index.html` for client-side routing, caches hashed `/assets/*` files for a year and sets basic security headers.

## Deploy from the CLI

```bash
cd frontend
vercel link
vercel env add VITE_RENOVA_DEMO production
vercel deploy --prod
```

To connect a real backend later, remove `VITE_RENOVA_DEMO` and set `VITE_API_BASE_URL` to the API root (for example `https://api.example.com/api`).
