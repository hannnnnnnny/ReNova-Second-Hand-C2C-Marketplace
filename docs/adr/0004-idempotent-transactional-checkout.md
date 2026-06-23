# ADR-0004: Make Checkout Idempotent and Transactional

## Status

Proposed

## Context

The current order transaction reads a listing without a database lock, marks it reserved, and creates an order. Concurrent buyers can both pass the status check. Repeated clicks can create duplicate orders. The current payment action is only a local state transition.

## Decision drivers

- One second-hand listing can have at most one active buyer reservation.
- Repeated requests must be safe.
- Correctness must hold across multiple API replicas.
- Payment state must come from a verified provider event.

## Considered options

### Frontend submit lock only

Improves UX but does not protect retries, multiple tabs, scripts, or network replay. Rejected.

### Distributed lock or Redis queue

Can serialize work but adds infrastructure and still needs database constraints. Rejected until measured need.

### MySQL row lock plus idempotency constraint

Uses the existing system of record, works across replicas, and directly protects the listing invariant. Selected.

## Decision

Lock the listing row during order creation, store a buyer-scoped idempotency key and payload fingerprint, create the reservation and order in one transaction, and return conflicts for incompatible races. Use optimistic versioning for later order transitions. Delete the fake pay endpoint; only a verified Stripe webhook may mark an order paid.

## Consequences

### Positive

- Double-clicks and request retries create one order.
- Two buyers cannot reserve the same listing.
- No Redis or custom distributed lock is required.
- Payment state has an auditable external source.

### Negative

- Hot listings can create lock contention.
- MySQL-specific integration tests are required.
- Stripe Connect introduces account, compliance, and fee obligations.

### Mitigations

- Keep the locked transaction small.
- Bound connection pools and return clear 409 responses.
- Measure contention with production-like load tests.

## Related decisions

- ADR-0001: Preserve Spring Boot + Vue 3 + MySQL Architecture
- `docs/aidlc/02-design.md`
