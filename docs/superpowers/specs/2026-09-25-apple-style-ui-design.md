# ReNova Apple-style UI

Supersedes the cream-and-green palette in `2026-09-13-marketplace-ui-design.md`; the structural work from that spec (accessible forms, native card links, loading/empty/error states) is kept.

## Direction

- Canvas `#f5f5f7`, white surfaces, near-black text `#1d1d1f`, a single accent: Apple blue `#0071e3` (`#0066cc` for link text to meet AA).
- Status colors use darker Apple variants (`#d70015`, `#248a3d`, `#b25000`) so text passes WCAG AA; they always sit next to a text label.
- No gradients, no heavy or offset shadows, no dashed frames, no rotated "sticker" decoration. Shadows: `0 2px 8px / 0 4px 12px / 0 8px 24px` at 4–12% black.
- Radii 8 / 12 / 16px; pill buttons; inputs are gray-filled with a 4px blue focus ring.
- System SF Pro stack with Segoe UI before CJK fallbacks (keeps Latin punctuation narrow on Windows).
- Motion uses `cubic-bezier(0.25, 0.1, 0.25, 1)` at 200ms/500ms; pressables scale to 0.96–0.98 on `:active`; card images zoom 3% → 8% on hover. `prefers-reduced-motion` disables transitions.
- Content width 1024px, 56–112px vertical page padding, sentence-case labels instead of tracked uppercase eyebrows.

## Implementation

Tokens live in `frontend/src/assets/styles.css` (plain CSS variables; no Tailwind). Shared account-form styles moved there from the login and signup pages. Every change is a separate commit on `feat/apple-style-ui`.

## Verification

Unit tests and production build pass. Home, browse, listing detail, profile, login, messages, orders and sell pages were checked at desktop width; home, browse, detail, login and signup have no horizontal overflow at 375px.
