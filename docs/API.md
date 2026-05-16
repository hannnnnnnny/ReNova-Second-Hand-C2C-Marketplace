# NovaCart API Reference

NovaCart exposes a RESTful JSON API under `/api`. Public storefront endpoints do not require authentication. Admin endpoints require `Authorization: Bearer <token>` from the admin login endpoint.

## Response Format

Successful response:

```json
{
  "success": true,
  "message": "Products loaded successfully.",
  "data": [],
  "timestamp": "2026-05-15T00:00:00Z"
}
```

Error response:

```json
{
  "success": false,
  "message": "Validation failed. Please review the highlighted fields.",
  "status": 400,
  "path": "/api/public/orders",
  "errors": [],
  "timestamp": "2026-05-15T00:00:00Z"
}
```

## Public Catalog

### List Categories

`GET /api/public/categories`

Returns active storefront categories ordered by name.

### List Products

`GET /api/public/products`

Optional query parameters:

- `search`: searches product name, description, brand, SKU, and category name.
- `categoryId`: filters active products by category.
- `minPrice`: filters products at or above this price.
- `maxPrice`: filters products at or below this price.
- `availableOnly`: when `true`, returns products with stock greater than zero.
- `sort`: supports `name`, `newest`, `price-low`, `price-high`, and `stock`.
- `page`: zero-based page index.
- `size`: page size, capped at 60.

Returns active storefront products with category context and pagination metadata.

### Get Product

`GET /api/public/products/{id}`

Returns one active product. Inactive or missing products return `404`.

## Public Orders

### Create Order

`POST /api/public/orders`

Creates an order and deducts stock in one transaction.

Request body:

```json
{
  "customerName": "Morgan Lee",
  "customerEmail": "morgan@example.com",
  "shippingAddress": "12 Market Street",
  "city": "Auckland",
  "postalCode": "1010",
  "country": "New Zealand",
  "shippingMethod": "STANDARD",
  "paymentMethod": "Demo Card Approved",
  "idempotencyKey": "checkout-unique-key",
  "simulatePaymentFailure": false,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

Important behavior:

- Empty carts are rejected.
- Invalid quantities are rejected.
- Inactive or missing products are rejected.
- Insufficient stock is rejected.
- Stock cannot become negative.
- Repeated submissions with the same `idempotencyKey` return the original order.
- `Demo Card Declined` or `simulatePaymentFailure: true` returns a safe demo payment failure without deducting stock.

Successful orders include an `orderNumber`, `paymentStatus`, `shippingMethod`, `subtotalAmount`, `shippingAmount`, `taxAmount`, `discountAmount`, and `totalAmount`.

### Get Order

`GET /api/public/orders/{id}`

Returns order confirmation data by order ID.

## Admin Authentication

### Login

`POST /api/admin/auth/login`

Request body:

```json
{
  "email": "admin@novacart.local",
  "password": "NovaCartAdmin123!"
}
```

Returns a bearer token, expiration metadata, admin email, and role.

## Admin Dashboard

### Metrics

`GET /api/admin/dashboard/metrics`

Returns product, category, order, revenue, low-stock, recent order, and inventory warning data.

## Admin Inventory

### Inventory Warnings

`GET /api/admin/inventory/warnings?threshold=5`

Returns products with stock at or below the threshold.

### Stock Movements

`GET /api/admin/inventory/movements`

Returns recent inventory events such as checkout stock deductions and cancellation restorations.

## Admin Categories

- `GET /api/admin/categories`
- `POST /api/admin/categories`
- `PUT /api/admin/categories/{id}`
- `DELETE /api/admin/categories/{id}`

Category request body:

```json
{
  "name": "Women",
  "slug": "women",
  "description": "Tailored separates, dresses, knitwear, and soft layers for everyday wardrobes.",
  "active": true
}
```

Deleting a category that has assigned products returns a business-rule error.

## Admin Products

- `GET /api/admin/products`
- `GET /api/admin/products/{id}`
- `POST /api/admin/products`
- `PUT /api/admin/products/{id}`
- `DELETE /api/admin/products/{id}`

Product request body:

```json
{
  "name": "Silk Wrap Blouse",
  "slug": "silk-wrap-blouse",
  "sku": "NC-SILK-WRAP-BLOUSE",
  "brand": "Aster Row",
  "description": "A fluid wrap blouse with a soft tie waist and subtle sheen.",
  "price": 118.00,
  "compareAtPrice": 148.00,
  "stockQuantity": 28,
  "lowStockThreshold": 6,
  "imageUrl": "/catalog/women.svg",
  "imageGallery": ["/catalog/women.svg"],
  "tags": ["spring-edit", "silk", "workwear"],
  "featured": true,
  "status": "ACTIVE",
  "active": true,
  "categoryId": 1
}
```

Admin product list endpoints support the same catalog query parameters as public products, plus `status` with `ACTIVE`, `DRAFT`, or `ARCHIVED`.

## Admin Orders

- `GET /api/admin/orders`
- `GET /api/admin/orders/{id}`
- `PATCH /api/admin/orders/{id}/status`

Status update body:

```json
{
  "status": "PROCESSING"
}
```

Allowed statuses:

- `PENDING`
- `PAID`
- `PROCESSING`
- `SHIPPED`
- `COMPLETED`
- `CANCELLED`

Status updates follow a fulfillment workflow:

| Current status | Allowed next statuses |
| --- | --- |
| `PENDING` | `PAID`, `PROCESSING`, `CANCELLED` |
| `PAID` | `PROCESSING`, `CANCELLED` |
| `PROCESSING` | `SHIPPED`, `CANCELLED` |
| `SHIPPED` | `COMPLETED` |
| `COMPLETED` | None |
| `CANCELLED` | None |

Submitting the same status again is accepted as an idempotent update. Terminal orders cannot be moved back into active fulfillment.

## Security Responses

- `401`: Missing, expired, or invalid bearer token.
- `403`: Authenticated account is not allowed to perform the action.
- `400`: Validation or business-rule failure.
- `404`: Requested resource was not found.
- `409`: Duplicate category or product slug.
