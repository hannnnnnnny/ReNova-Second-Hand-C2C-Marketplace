# ReNova API Reference

ReNova exposes a REST JSON API under `/api`. Public discovery endpoints are open. Marketplace actions require the signed `RENOVA_SESSION` HttpOnly cookie. State-changing requests also require the `XSRF-TOKEN` cookie value in the `X-XSRF-TOKEN` header.

The frontend API layer in `frontend/src/api/endpoints.js` maps directly to these routes. There are no frontend mock fallbacks for marketplace trading flows.

## Response Format

Successful response:

```json
{
  "success": true,
  "message": "Listings.",
  "data": {},
  "timestamp": "2026-05-28T00:00:00Z"
}
```

Error response:

```json
{
  "success": false,
  "message": "Validation failed. Please review the highlighted fields.",
  "status": 400,
  "path": "/api/listings",
  "errors": [
    {
      "field": "title",
      "message": "must not be blank"
    }
  ],
  "timestamp": "2026-05-28T00:00:00Z"
}
```

Validation errors include field-level messages. Invalid enum values return a `400` business error instead of an internal server error.

## Authentication

### Sign Up

`POST /api/auth/signup`

```json
{
  "email": "seller@example.com",
  "displayName": "Morgan Lee",
  "password": "<your-password>",
  "location": "Auckland, NZ"
}
```

### Log In

`POST /api/auth/login`

```json
{
  "email": "seller@example.com",
  "password": "<your-password>"
}
```

Sets the HttpOnly session cookie and returns expiration metadata plus the authenticated user summary. The JWT is never returned in JSON.

### CSRF Token

`GET /api/auth/csrf`

Issues the readable `XSRF-TOKEN` cookie used by browser clients. Axios sends its value as `X-XSRF-TOKEN` on writes.

### Current Session

`GET /api/auth/me`

Requires the valid session cookie.

### Log Out

`POST /api/auth/logout`

Expires the server-authentication cookie and requires a valid CSRF token.

## Public Catalog

### Categories

`GET /api/public/categories`

Returns seeded listing categories ordered by display order.

### Search Listings

`GET /api/public/listings`

Query parameters:

- `keyword`: searches listing title and description.
- `categoryId`: filters by category ID.
- `minPrice`: filters at or above this price.
- `maxPrice`: filters at or below this price.
- `condition`: one of `NEW`, `LIKE_NEW`, `GOOD`, `FAIR`, `FOR_PARTS`.
- `location`: searches listing location.
- `sort`: `newest`, `price_asc`, `price_desc`, or `popular`.
- `page`: zero-based page index.
- `size`: page size, capped at 60.

Only active listings are returned.

### Listing Detail

`GET /api/public/listings/{id}`

Returns listing detail, seller public profile, category, images, counters, and whether the current user has favorited it when a valid token is present.

### Public User Profile

- `GET /api/public/users/{id}`
- `GET /api/public/users/{id}/listings`
- `GET /api/public/users/{id}/reviews`

Public user responses intentionally omit private fields such as email.

## Listings

Listing endpoints require authentication.

- `POST /api/listings`
- `PUT /api/listings/{id}`
- `DELETE /api/listings/{id}`
- `GET /api/listings/mine`
- `POST /api/listings/{id}/favorite`
- `GET /api/listings/favorites`

Create listing request:

```json
{
  "title": "Oak side table",
  "description": "Solid wood, light wear on the top.",
  "price": 42.5,
  "originalPrice": 80,
  "condition": "GOOD",
  "categoryId": 3,
  "location": "Auckland, NZ",
  "negotiable": true,
  "shippingFee": 0,
  "mediaIds": [41, 42]
}
```

Update listing accepts partial fields plus `status`. `mediaIds` must contain one to eight completed images owned by the seller. Listings marked `SOLD` must move through order completion, not manual editing.

## Listing Media

Media mutation endpoints require authentication. Stored objects remain private; public listing image URLs redirect to short-lived signed reads.

1. `POST /api/media/upload-intents` with `fileName`, `contentType`, and `sizeBytes`.
2. `PUT` the file to the returned `uploadUrl` with every returned `requiredHeaders` value.
3. `POST /api/media/{id}/complete` to decode, validate, and normalize the image.
4. Include the completed media ID in the listing's `mediaIds` array.

Accepted formats are JPEG, PNG, and WebP. Each source image is limited to 10 MB and 20 megapixels; listings accept at most eight images.

## Offers

Offer endpoints require authentication.

- `POST /api/offers`
- `GET /api/offers/{id}`
- `POST /api/offers/{id}/accept`
- `POST /api/offers/{id}/reject`
- `POST /api/offers/{id}/counter`
- `POST /api/offers/{id}/withdraw`
- `POST /api/offers/{id}/accept-counter`
- `GET /api/offers/received`
- `GET /api/offers/sent`

Offers enforce seller/buyer ownership rules and listing availability.

## Conversations

Conversation endpoints require authentication.

- `GET /api/conversations`
- `POST /api/conversations`
- `GET /api/conversations/{id}`
- `POST /api/conversations/{id}/messages`
- `GET /api/conversations/unread-count`

Conversations are scoped to listing participants.

## Orders

Order endpoints require authentication.

- `POST /api/orders`
- `GET /api/orders/{id}`
- `POST /api/orders/{id}/ship`
- `POST /api/orders/{id}/confirm-receipt`
- `POST /api/orders/{id}/cancel`
- `GET /api/orders/buying`
- `GET /api/orders/selling`

`POST /api/orders` requires an `Idempotency-Key` header containing a canonical UUID. Replaying the same key and payload returns the original order; reusing the key for a different payload returns `409 Conflict`. Creating an order reserves the listing for a configurable payment window. ReNova does not expose a client-triggered payment endpoint; only a future verified provider webhook may mark an order paid.

Create order request:

```json
{
  "listingId": 1,
  "acceptedOfferId": null,
  "shippingName": "Morgan Lee",
  "shippingPhone": "+64 20 0000 0000",
  "shippingAddress": "12 Market Street, Auckland",
  "buyerNote": "Please ship this week."
}
```

Current payment behavior is a recorded marketplace state transition, not an external card processor. Real card payments require integrating a payment provider and webhook reconciliation.

## Reviews And Profile

- `POST /api/reviews`
- `GET /api/orders/{orderId}/reviews`
- `GET /api/users/me`
- `PUT /api/users/me`

Reviews can be created after an order is completed and are tied to buyer/seller roles.

## Operational Notes

- Runtime database: MySQL.
- Test database: H2 in MySQL compatibility mode.
- Authentication: stateless JWT in an HttpOnly cookie, SPA CSRF protection, and BCrypt password hashes.
- Public seed data exists for local development and portfolio preview only.
- High concurrency requires deployment support: CDN, load balancer, horizontal backend replicas, database sizing, queue/rate-limit strategy, observability, and load testing.
