# Marketplace UI Implementation Plan

> **For agentic workers:** Use focused parallel tasks for the independent form and card components, with root integration and review.

**Goal:** Refine ReNova's first impression and make browsing and account forms clearer.

**Architecture:** Preserve Vue, Pinia, Vue Router and the current API. Shared visual tokens remain in styles.css; page-specific composition belongs beside its Vue component. Localized UI refinements live in small en/zh bundles merged into the existing messages.

**Tech Stack:** Vue 3, Vite 7, Vitest, vanilla CSS, existing lucide-vue-next icons, Azure Container Apps.

## Global constraints

Preserve cream/green identity, existing functionality and production/demo split. No invented inventory or account credentials. Support 375px, keyboard focus, reduced motion, readable contrast, and English/Chinese. Use existing dependencies. Changes stay on the existing codex/azure-deployment branch.

## Tasks

- [x] Account forms: LoginPage.vue, SignupPage.vue, new components/AuthLayout.vue and PasswordField.vue as useful; form-specific localization module. Implement inline errors, labels, autocomplete, password visibility, and compact balanced layout. Verify invalid/valid submit, pending state, demo visibility and safe redirects.
- [x] Product cards: ListingCard.vue with scoped styles; card localization module. Replace click-only article with native RouterLink, separate favorites, consistent 4:5 imagery and accessible empty image fallback. Verify links, image failure and favorite behavior.
- [x] Homepage: HomePage.vue, homepage localization, known-category translation utility. Implement compact editorial hero, category strip, skeletons, error retry, empty CTA and honest workflow copy. Verify API rejection does not render empty state and retry restores results.
- [x] Shared presentation: styles.css, AppHeader.vue, AppFooter.vue, i18n/messages.js. Refine fonts, palette, borders, focus, buttons, responsive navigation and grid. Integrate scoped localization exports into both locales.
- [x] Verify: run `npm --prefix frontend run test:unit` and `npm --prefix frontend run build`; exercise local demo and production API modes at desktop and 375px; inspect screenshots, inline errors, keyboard links and loading/empty/error states.
- [ ] Release: build and push frontend image tagged azure-20260915-ui, update deployment script/docs and Azure web container only, verify healthy revision and live page. Commit with a clear message and push the existing deployment branch.

## Review points

Root reviews each independent diff for scope, correct translation integration, input/redirect validation and preserved API contracts. Avoid tests that merely assert class names: verify behavior using existing unit infrastructure and browser interaction. Record exact verification outcomes after completion.


## Verification outcomes

- 59 unit tests passed, including homepage partial failure/retry/pending behavior, safe auth redirects, field validation messages and recursive locale coverage. Production build passed.
- Browser screenshots checked desktop homepage, populated cards, 375px homepage and login, desktop inline login error and 375px signup. Mobile document width was 360px within a 375px viewport.
- Local demo sign-in and password visibility passed. Production-mode login hides demo controls; a controlled 401 renders a persistent inline message, and the signup/login link retains `/browse` as the return destination.
- A local API fixture returned 503 for homepage listings while categories succeeded; the error showed Retry rather than empty inventory. Switching the fixture to HTTP 200 with zero listings and clicking Retry restored the purposeful empty state.
- Independent account/card review found a validation-message gap, fixed with backend-aligned limits and localized field guidance. Final integrated review approved with no further actionable findings.
- Azure deployment was briefly paused to investigate cost. MySQL autoscale I/O was identified as the source of NZD 5.66 usage through 14 September and disabled in separate commit `1ae57e4`; the server and live categories endpoint were verified healthy.

- 15 September: user explicitly requested Azure shutdown. Release cancelled; UI changes remain local and uncommitted. Current deployed frontend tag remains azure-20260909-login.
