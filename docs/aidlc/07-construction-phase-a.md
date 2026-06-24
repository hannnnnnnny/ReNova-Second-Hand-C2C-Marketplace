# Construction Phase A: Correctness

Date: 2026-06-24

Status: Complete, awaiting owner review

## Delivered core loop

The persisted ReNova flow now covers:

1. Register and sign in with the existing BCrypt and HttpOnly-cookie security model.
2. Request a short-lived upload intent and upload directly to private S3-compatible storage.
3. Decode, validate, pixel-limit, and metadata-strip JPEG, PNG, or WebP source bytes on the server.
4. Create or edit a listing using one to eight seller-owned media IDs.
5. Browse, search, and view database-backed listings and signed private image reads.
6. Start a persisted buyer/seller conversation.
7. Create an idempotent purchase reservation under a pessimistic listing lock.
8. Cancel abandoned pending orders in bounded batches and release the listing.

## Removed false behavior

- The browser can no longer submit arbitrary listing image URLs.
- Checkout no longer searches only the first page of sent offers or silently falls back to list price.
- `POST /api/orders/{id}/pay` and its frontend button were deleted. ReNova does not claim an order is paid without a verified payment provider webhook.
- Duplicate order submission and two-buyer reservation races no longer create two active purchases.

## Storage boundary

Production code uses the real MinIO Java SDK through a small `MediaObjectStorage` port. Tests replace only this external network boundary; image decoding, ownership, database attachment, API authorization, listing creation, conversation creation, and order creation execute through real application code and H2 persistence.

Docker Compose now includes a pinned MinIO service. Credentials, bucket name, browser upload endpoint, internal endpoint, and public API base URL are environment-driven.

## Verification output

```text
Backend: Tests run: 56, Failures: 0, Errors: 0, Skipped: 0
Frontend: Test Files 6 passed; Tests 28 passed
Frontend production build: 1797 modules transformed; build completed
Docker Compose: config --quiet exited 0 with temporary non-production values
git diff --check: exited 0
```

The core-flow integration test proves register/login, upload validation, listing persistence, search, detail, contact, and purchase reservation as one continuous API flow. Focused tests also prove image-format spoofing rejection, media ownership, idempotent replay, simultaneous-buyer exclusion, reservation expiry, and absence of the fake pay route.

## Deliberate boundary

Live payment remains unavailable. Stripe Connect or another marketplace provider requires the owner's account, KYC, test credentials, webhook configuration, fee model, refund policy, and liability decisions. Adding a local state-changing substitute would recreate the fake-payment defect this phase removed.

Per the approved AIDLC sequence, Phase B (Structure) starts only after owner approval.
