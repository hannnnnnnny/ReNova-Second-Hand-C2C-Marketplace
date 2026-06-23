# ADR-0002: Use Cookie Authentication and Actor-Scoped Authorization

## Status

Accepted

## Context

ReNova currently returns a JWT to browser JavaScript and stores it in `localStorage`. Route guards improve navigation but cannot enforce access. Service methods contain useful ownership checks, yet denial semantics and direct cross-user tests are inconsistent.

## Decision drivers

- Authentication material must not be readable by browser JavaScript.
- Every private read and privileged mutation must be enforced by the server.
- The design must remain stateless enough for horizontal Spring Boot replicas.
- Authorization rules must be named and testable rather than duplicated across controllers.

## Considered options

### Keep bearer JWT in localStorage

Simple and already implemented, but any XSS can steal the token. Rejected.

### Server-side sessions

Strong revocation and simple browser behavior, but shared session storage would add stateful infrastructure before it is justified. Deferred.

### JWT in HttpOnly cookie with CSRF protection

Keeps replicas stateless, removes JavaScript token access, and fits the existing JWT implementation. Requires CSRF, exact credentialed CORS, and secure cookie configuration.

## Decision

Use a short-lived JWT in an `HttpOnly`, production-`Secure`, `SameSite=Lax` cookie. Enable CSRF tokens for state-changing requests. Load private resources through actor-scoped repository methods and use Spring method security for admin roles.

## Consequences

### Positive

- XSS cannot directly read the session token.
- Backend policy remains authoritative.
- Cross-user behavior can be proven through direct API tests.
- API replicas remain stateless.

### Negative

- Frontend and backend CSRF handling must change together.
- Cross-origin development needs exact credentialed CORS configuration.
- Logout expires the cookie but does not revoke an already stolen token before expiry.

### Mitigations

- Keep token lifetime bounded.
- Add CSP and avoid raw HTML sinks.
- Use 404 for inaccessible private objects and 403 for visible resources the actor cannot mutate.

## Related decisions

- ADR-0001: Preserve Spring Boot + Vue 3 + MySQL Architecture
- `docs/aidlc/02-design.md`
