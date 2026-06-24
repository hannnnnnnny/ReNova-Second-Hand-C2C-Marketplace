# Construction Gate 2: Server-Side Authorization

Date: 2026-06-24

Status: PASS

This increment changes authentication transport and authorization only. It does not add marketplace features.

## Findings fixed

1. The browser stored a bearer JWT in `localStorage`, where any successful XSS could read it.
2. CSRF protection was disabled while the API accepted browser credentials.
3. Any authenticated user could request `/api/orders/{orderId}/reviews`; the service loaded the order without checking participation.
4. Listing, offer, order, and conversation services commonly loaded a resource by ID first and then performed scattered ID comparisons. Denials were frequently reported as business-rule `400` responses rather than authorization failures.
5. `/api/admin/**` had no explicit server-side role boundary.
6. Login accepted an unchecked redirect string from the URL.

## Authentication boundary: before and after

### Before

- Login and signup returned the signed JWT in the JSON body.
- Vue saved the JWT and user object in `localStorage`.
- Axios attached `Authorization: Bearer ...` from JavaScript.
- CSRF was disabled.
- CORS accepted the `Authorization` header and did not use credentialed requests.

### After

- Login and signup return only `expiresAt` and the current user.
- The signed JWT is written only to `RENOVA_SESSION`, with `HttpOnly`, `SameSite=Lax`, path `/api`, and configurable `Secure`.
- The backend authenticates only from `RENOVA_SESSION`; a copied bearer header is rejected.
- Spring Security 7 SPA CSRF protection uses the `XSRF-TOKEN` cookie and `X-XSRF-TOKEN` header.
- Axios uses credentialed requests and the standard CSRF cookie/header pair. No auth token or user profile is persisted in browser storage.
- Logout expires the session cookie server-side.
- CORS uses exact configured origins, credentials, and only the required request headers.
- Login redirects accept only local paths beginning with one `/`.

## Protected endpoints: before and after

| Endpoint or action | Before | After |
|---|---|---|
| `PUT /api/listings/{id}` | Loaded any listing, compared seller IDs, returned `400` | Public resource remains loadable; a non-owner receives explicit server-side `403` |
| `DELETE /api/listings/{id}` | Loaded any listing, compared seller IDs, returned `400` | Non-owner receives server-side `403`; listing is unchanged |
| Offer accept/reject/counter | Loaded any offer, then checked seller ID | Repository query requires both offer ID and current seller; mismatch is `404` |
| Offer withdraw | Loaded any offer, then reconstructed authorship with a boolean branch | One actor-scoped query covers buyer offers and seller counters; mismatch is `404` |
| Offer counter acceptance | Loaded any offer, then checked buyer ID | Repository query requires offer ID and current buyer; mismatch is `404` |
| Order creation with accepted offer | Loaded any offer, then checked buyer ID | Offer query is scoped to the current buyer before order data is used |
| `GET /api/orders/{id}` | Loaded any order, then checked participant IDs | Repository query requires current buyer or seller; outsiders receive `404` |
| Order pay / confirm receipt | Loaded any order, then checked buyer ID | Repository query requires the current buyer; seller or outsider receives `404` |
| Order ship | Loaded any order, then checked seller ID | Repository query requires the current seller; buyer or outsider receives `404` |
| Order cancel | Loaded any order, then checked participant IDs | Repository query requires current buyer or seller; outsider receives `404` |
| `GET /api/orders/{id}/reviews` | No participant authorization | Reuses the order participant query; outsider receives `404` |
| Review creation | Loaded any order before deriving reviewer role | Requires the current user to be an order participant before reviewing |
| Conversation read/send | Loaded any conversation, then checked participant IDs | Repository query requires current buyer or seller; outsider receives `404` |
| `GET/PUT /api/users/me` | Relied on the global authenticated route rule | Still server-authenticated; identity comes only from the validated session, never a requested user ID |
| `/api/admin/**` | Fell through to any authenticated user | Requires `ROLE_ADMIN` before controller routing; normal users receive `403` |
| Purchase and all other writes | Authentication only; CSRF disabled | Valid session plus CSRF cookie/header required server-side |

Private resources deliberately return `404` for a user outside the resource boundary, so the API does not confirm that another user's order, offer, or conversation ID exists. Public listings return `403` for unauthorized mutation because their existence is already public.

## Executable proof

`ServerAuthorizationTests` performs direct API calls with three distinct accounts and proves:

- user A cannot update or delete user B's listing;
- the failed actions do not change the listing title or status;
- an outsider cannot read another pair's order, conversation, or order reviews;
- an outsider cannot accept another user's offer;
- a buyer cannot call the seller-only shipping action;
- a seller cannot call the buyer-only payment action;
- `/api/users/me` returns only the authenticated user;
- public profiles expose no email or password fields;
- a normal user cannot access `/api/admin/**`;
- missing CSRF is rejected; and
- a bearer token header is not accepted.

`CsrfCookieIntegrationTests` uses the real cookie repository without Spring's CSRF test shortcut and proves the browser flow: obtain `XSRF-TOKEN`, send its raw value in `X-XSRF-TOKEN`, create a session, read `/api/auth/me`, and expire the session through logout.

Verification output:

```text
Backend:  Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
Frontend: Test Files 5 passed; Tests 24 passed
Frontend production build: 138 modules transformed; built successfully
```

## Gate decision

PASS. Protected actions are now enforced by the server, cross-user direct API calls are covered by executable tests, and the frontend route guard is only a user-experience convenience.
