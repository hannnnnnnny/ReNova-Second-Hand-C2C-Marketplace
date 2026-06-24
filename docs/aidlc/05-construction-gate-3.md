# Construction Gate 3: Secrets And Git History

Date: 2026-06-24

Status: PASS WITH ROTATION REQUIRED

This report intentionally does not repeat exposed credential values. Git history remains readable, so repeating them in new documentation would create another live copy.

## Scan coverage

- Gitleaks 8.30.1 directory scan of the current 1.29 MB worktree.
- Gitleaks Git scan of all reachable branches and tags: 145 commits, approximately 2.71 MB of patch data.
- Targeted full-history searches for AWS, GitHub, Stripe, Slack, Google, OAuth, private-key, credentialed-URL, database-password, JWT-secret, and known project credential patterns.
- Full-history filename search for `.env`, PEM/key files, PKCS stores, Java keystores, SSH keys, and credential files.
- Current-source searches excluding build output and dependencies.

## Secrets found

### 1. Legacy administrator credential

- First committed in `3c773bd03ed43a122972c7ffa278a7814b171dee`.
- Initially stored in `backend/src/main/java/com/novacart/store/config/DataInitializer.java` for `admin@novacart.local`.
- It was later repeated in `README.md`, `docs/API.md`, backend tests, and `frontend/src/pages/admin/AdminLoginPage.vue`.
- Exposed in Git history: **YES**.
- Current tree: removed before this gate.
- Required action: if that account or password ever reached any database or deployment, delete or rotate it. Gate 1 deactivates known legacy accounts outside the explicit demo profile, but that does not erase external deployments.

### 2. Legacy ReNova demo-account credential

- First committed in `65ad4994452814fee0b3c18792c85f3dd02aee26`.
- Shared by `ava@renova.local`, `liam@renova.local`, `nora@renova.local`, and `sam@renova.local` in the old `DataInitializer`.
- It was also present in `README.md`, login/footer UI, tests, API docs, and committed built assets from `c71a8972b2acc4bc18c935053fbaed2c78420113`.
- Exposed in Git history: **YES**.
- Current tree: removed before this gate.
- Required action: delete or rotate those accounts anywhere they were deployed. Gate 1 deactivates known legacy accounts outside demo mode.

### 3. Application database password default

- First committed in `8a7c16df60496a6f20806098ecf352235b326e0b` in `backend/.env.example` and `backend/src/main/resources/application.yml`.
- Repeated in README revisions, `docker-compose.yml` from `221b2d9a6fc1cc1ea5bf05da083a43692823fe89`, and `docker.env.example` from `51891d6c0d0017278d73c5b40f4da1f52d135fb3`.
- Exposed in Git history: **YES**.
- Current tree: removed; `DB_USERNAME` and `DB_PASSWORD` are required environment variables.
- Required action: rotate the application database password if any environment used the old default.

### 4. MySQL root password default

- First committed in `221b2d9a6fc1cc1ea5bf05da083a43692823fe89` in `docker-compose.yml`.
- Repeated in `docker.env.example` from `51891d6c0d0017278d73c5b40f4da1f52d135fb3`.
- Exposed in Git history: **YES**.
- Current tree: removed; Compose requires `MYSQL_ROOT_PASSWORD`.
- Required action: rotate it for every Docker/MySQL environment that used the old default.

### 5. JWT signing-secret fallbacks

- The backend fallback was first committed in `8a7c16df60496a6f20806098ecf352235b326e0b` in `application.yml`.
- The Docker fallback was first committed in `221b2d9a6fc1cc1ea5bf05da083a43692823fe89` and repeated in `docker.env.example` from `51891d6c0d0017278d73c5b40f4da1f52d135fb3`.
- Exposed in Git history: **YES**.
- Current tree: removed; `JWT_SECRET` is required at runtime.
- Required action: rotate the JWT secret in every existing environment and invalidate all sessions signed with an old fallback.

## Not found

- No high-confidence secret detected by Gitleaks in the worktree or history.
- No AWS, GitHub, Stripe, Slack, Google, or OAuth token pattern.
- No private-key marker or credential store.
- No URL containing embedded username/password credentials.
- No committed `.env`; only `backend/.env.example` and `frontend/.env.example` have ever been tracked.

This cannot prove that a credential was never shared outside Git. It proves only what is reachable in this repository and its available Git refs.

## Fixes

- `application.yml` has no database credential or JWT fallback. Missing values stop startup.
- `docker-compose.yml` uses required-variable interpolation for database user/password, root password, and JWT secret.
- `.env.example` files contain intentional empty values only; no runnable credentials.
- `.env` and `.env.*` remain ignored except `.env.example`.
- Private key and keystore extensions are ignored.
- README, API, architecture, and deployment docs no longer publish the removed defaults or obsolete bearer-token workflow.
- Production documentation requires `SESSION_COOKIE_SECURE=true` behind HTTPS.

## Executable proof

```text
Gitleaks worktree: scanned ~1.29 MB; no leaks found
Gitleaks history:  scanned 145 commits / ~2.71 MB; no leaks found
Backend tests:     14 run, 0 failures, 0 errors, 0 skipped
```

Fail-closed checks:

- `docker compose config` without an ignored `.env` fails on the first missing required credential.
- `java -jar backend-0.0.1-SNAPSHOT.jar` without secrets fails because `JWT_SECRET` cannot be resolved.
- `git check-ignore` confirms root, backend, and frontend `.env` paths are ignored.
- Compose validates successfully when process-local validation values are supplied.

## Rotation checklist

1. Rotate every deployed `JWT_SECRET`; this invalidates old sessions.
2. Rotate application and MySQL root database passwords anywhere old defaults may have been used.
3. Delete or reset the legacy admin and four demo accounts in every existing database.
4. Update the hosting provider's secret manager and restart the backend.
5. Do not rely on deleting values from the current branch; they remain in old commits unless repository history is rewritten.

History rewriting is deliberately not performed here. It is destructive for collaborators and does not replace rotation, because existing clones and forks retain the old objects.

## Gate decision

PASS WITH ROTATION REQUIRED. The current tree contains no detected secret and has no runnable secret fallback. The five credential groups above are explicitly exposed in history and must be treated as compromised if they were ever used.
