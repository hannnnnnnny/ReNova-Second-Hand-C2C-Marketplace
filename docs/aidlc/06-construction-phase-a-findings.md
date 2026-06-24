# Construction Phase A: Correctness Findings

Date: 2026-06-24

Status: Resolved and verified in Construction Phase A

## Scope

This phase covers the persisted marketplace loop only: register/login, upload listing images, create/search/view a listing, contact the seller, and create a purchase order. Structure work, admin UI, motion, production deployment, and unverified payment-provider work remain outside this increment.

## Confirmed defects

### A-01: Listing images are arbitrary external strings

- `ListingCreateRequest` and `ListingUpdateRequest` accept `imageUrls` supplied by the browser.
- The post/edit pages ask users to paste URLs instead of uploading files.
- The server does not own, decode, verify, re-encode, expire, or clean up these objects.
- A seller can attach HTML endpoints, tracking URLs, unstable hosts, or another person's object.

Fix target: owned media records, short-lived presigned S3-compatible upload intents, server-side image decoding/re-encoding, and listing attachment by `mediaIds` only.

Resolution: implemented with private S3-compatible storage, owned media rows, JPEG/PNG/WebP decoding, pixel and byte limits, metadata-stripping re-encoding, expiry cleanup, and seller-owned `mediaIds`.

### A-02: Checkout is neither idempotent nor race-safe

- `POST /api/orders` has no idempotency key.
- The listing is loaded without a pessimistic database lock.
- There is no active-order check under a lock.
- Replays can create duplicates and concurrent buyers can pass availability checks on separate replicas.
- Mutable orders have no optimistic version.

Fix target: buyer-scoped UUID idempotency key plus request fingerprint, listing row lock, active-order conflict check, atomic reservation, reservation expiry, and optimistic order versioning.

Resolution: implemented and tested with simultaneous buyers; exactly one reservation succeeds.

### A-03: Payment is a fake client-triggered state transition

- `POST /api/orders/{id}/pay` marks an order paid without a payment provider, signature, money movement, or webhook.
- The frontend presents this as a payment button.
- A real deployment could therefore claim a paid order without receiving funds.

Fix target: delete the endpoint and UI. Orders may be created and persisted, but cannot become paid until a verified provider integration exists. No live-payment claim will be made in this phase.

Resolution: the endpoint, frontend action, and misleading payment copy were removed.

### A-04: Accepted-offer checkout can silently use the wrong price

- Checkout downloads only the first 100 sent offers and searches that page in the browser.
- An older accepted offer can be omitted.
- When the requested offer is missing, the UI silently falls back to list price while retaining the checkout route.

Fix target: add a buyer/seller-scoped single-offer read and require an exact accepted offer before rendering its checkout price. The server remains the final price authority.

Resolution: checkout now loads the exact actor-scoped offer and refuses mismatched or non-accepted offers.

### A-05: Unpaid reservations never expire

- Creating an order marks the one-off listing `RESERVED` indefinitely.
- A buyer who abandons checkout can prevent every future buyer from purchasing.

Fix target: persist `reservationExpiresAt`, reject expired order actions, and release expired reservations with a bounded scheduled database job.

Resolution: pending orders receive a configurable expiry and a locked, bounded scheduled batch cancels them and releases the listing.

### A-06: Existing tests prove pieces, not the complete core loop

- Security and listing creation have meaningful tests.
- There is no single test proving registration through image attachment, browse/view, conversation, and idempotent order creation against persisted data.

Fix target: add a database-backed core-flow integration test plus focused media-validation and checkout-concurrency tests. Test doubles are allowed only at the external object-storage network boundary.

Resolution: the persisted core-flow test covers register/login, upload intent, image normalization, publish, search, detail, conversation, and order reservation. Focused tests cover format spoofing, ownership, replay, concurrency, and expiry.

## Explicit non-fixes

- Stripe Connect cannot be claimed or verified without owner-controlled account/KYC, test secrets, webhook endpoint, and acceptance of marketplace fees and liability.
- The fake payment path will be removed rather than replaced with another placeholder.
- Production CDN, object-storage provider, and load-test sizing remain Operations decisions. Local development will use MinIO through Docker Compose.
