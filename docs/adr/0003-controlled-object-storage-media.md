# ADR-0003: Use Controlled Object Storage for Listing Media

## Status

Proposed

## Context

Listings currently accept arbitrary image URL strings. That does not provide upload ownership, content validation, lifecycle cleanup, reliable availability, or a scalable media delivery path.

## Decision drivers

- Buyers must see genuine uploaded product images.
- Users must not attach another user's uploads.
- Application replicas must not depend on local disks.
- Large image bytes should not pass through every application request.
- Local development must remain reproducible.

## Considered options

### Keep external URLs

Lowest effort, but insecure and unreliable. Rejected.

### Store image blobs in MySQL

Transactional but expensive for database backups, delivery, and scaling. Rejected.

### S3-compatible object storage with upload intents

Separates media delivery from application replicas, supports presigned direct upload, and works with MinIO locally. Selected.

## Decision

Create owned `MediaAsset` records and short-lived presigned upload intents. Verify and re-encode uploaded JPEG, PNG, and WebP files before they can be attached to a listing. Serve approved objects through a CDN with fixed content types.

## Consequences

### Positive

- Real upload UX with ownership and validation.
- Stateless application replicas and CDN-friendly media.
- Orphan cleanup and listing image order are explicit.

### Negative

- Adds object-storage configuration and MinIO to local Compose.
- Completion verification consumes bounded backend CPU and storage I/O.
- Production storage and egress may cost money.

### Mitigations

- Strict size, dimension, count, and MIME limits.
- Rate-limit upload intents and completion.
- Add asynchronous processing only if measured load requires it.

## Related decisions

- ADR-0001: Preserve Spring Boot + Vue 3 + MySQL Architecture
- `docs/aidlc/02-design.md`
