# NovaCart Architecture

NovaCart is organized as a full-stack ecommerce application with a clear separation between public shopping features, protected merchant administration, and backend business rules.

## Application Boundaries

- Public storefront: Browsing products, viewing categories, managing a local cart, and placing orders.
- Admin workspace: Managing products, categories, inventory, orders, dashboard metrics, and authentication.
- Backend API: Serving RESTful JSON endpoints, enforcing validation, protecting admin operations, applying stock and order rules, and persisting data in MySQL.

## Backend Layers

- Controllers expose REST endpoints and handle request boundaries.
- DTOs define public request and response contracts.
- Services contain business logic and transaction boundaries.
- Repositories provide persistence access through Spring Data JPA.
- Entities model persisted data and relationships.
- Security components handle JWT authentication and admin access control.
- Exception handlers convert application errors into consistent JSON responses.

## Frontend Layers

- API modules wrap HTTP access and authentication headers.
- Router modules define public and admin navigation.
- Stores keep cart, authentication, and shared UI state.
- Layouts separate storefront and admin page chrome.
- Components provide reusable UI building blocks.
- Pages implement route-level workflows and states.

## Data Model Direction

The initial domain model will include admins, categories, products, orders, and order items. Inventory updates will be handled during checkout to prevent negative stock and inconsistent orders.
