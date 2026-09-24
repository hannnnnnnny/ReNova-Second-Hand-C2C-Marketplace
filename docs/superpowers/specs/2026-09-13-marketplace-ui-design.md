# ReNova UI refinement

The user approved the six-point UI proposal with “start do it”. Retain the cream and green identity; refine the existing Vue app without changing backend contracts or adding fabricated production listings.

## Approved design

- Use warm neutral surfaces, dark green primary actions, lighter borders and soft shadows. Keep rounded headings while using readable system fonts for English and Chinese body text. Visible keyboard focus and reduced-motion support apply throughout.
- Shorten the homepage hero, keep categories compact, bring listings earlier in the page. Keep the existing photography as editorial imagery rather than implying those items are for sale. Show separate loading skeletons, inline errors with retry, and a purposeful empty state with a listing CTA.
- Use a consistent 4:5 product frame, stronger prices, condition labels and secondary locations. Preserve image content and native link behavior, and keep favorite controls separate from navigation.
- Tighten login and signup layouts, associate labels, enable autocomplete, add password visibility controls, and retain errors inline. Preserve production/demo separation and existing authentication.
- Localize new copy and known category names in English and Chinese. Remove misleading claims about real payment protection: this portfolio's payments and shipping are simulated.

## Acceptance

Desktop and 375px layouts have no horizontal overflow. Forms retain validation and valid redirects. Product links work with keyboard and new-tab navigation. Loading, empty, failure and populated states are verified; existing unit tests and production build pass. Deploy the validated frontend to the existing Azure Container App and verify the live revision. No new Azure resources or cost tiers are needed.
