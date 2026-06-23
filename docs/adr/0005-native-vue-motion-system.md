# ADR-0005: Use Native Vue Motion with an Accessible Catalogue Direction

## Status

Proposed

## Context

ReNova is functional but visually static. The user requested useful features and effects inspired by 21st.dev. The referenced components use React-oriented tooling, while ReNova uses Vue 3 and plain CSS. Motion must not reduce legibility or make operational screens theatrical.

## Decision drivers

- Motion must explain state and continuity.
- The interface must remain usable by older adults and keyboard users.
- Layout shift and long animation chains are unacceptable in marketplace workflows.
- A new animation dependency needs evidence, not enthusiasm.

## Considered options

### Copy 21st.dev component implementations

Wrong framework, unclear fit, and unnecessary source coupling. Rejected.

### Add a general animation library immediately

Capable but adds bundle and maintenance cost before native Vue limits are known. Deferred.

### Vue transitions, CSS, and small composables

Matches the stack, supports reduced motion, and is enough for the planned card, gallery, filter, dialog, and route effects. Selected.

## Decision

Use Vue `Transition`/`TransitionGroup`, CSS motion tokens, and a `useReducedMotion` composable. Adapt Product Card, Expandable Card, Shine Hover, carousel, and gallery ideas to real ReNova workflows. Self-host Atkinson Hyperlegible for body copy and Newsreader for display type.

## Consequences

### Positive

- Small bundle impact and no React-specific dependency.
- Motion behavior is centrally tokenized and testable.
- Reduced-motion behavior is built into every effect.
- Typography directly supports the older-adult usability goal.

### Negative

- Complex gesture animation would require custom work.
- Native transitions need disciplined component boundaries.
- Font assets increase the application bundle.

### Mitigations

- Keep effects short and state-driven.
- Subset and preload only required font files.
- Add an animation library later only for a documented interaction that native tools cannot implement well.

## Related decisions

- ADR-0001: Preserve Spring Boot + Vue 3 + MySQL Architecture
- `docs/aidlc/02-design.md`
