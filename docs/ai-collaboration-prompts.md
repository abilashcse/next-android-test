# Reusable AI collaboration prompts (Lead Android Engineer)

This document collects **prompt templates** used while shaping this codebase (including **product search** on DummyJSON `GET /products/search`, **app-wide offline** via `NetworkMonitor` + full-screen gate in `AppScaffold`, and **shared PLP/search grids**). Treat them as **starting points**: paste into your assistant, fill the placeholders, and **adapt constraints** (libraries, APIs, naming) to the target project. The shipped app combines assistant output with **human review and refactors**—these prompts are tooling, not a substitute for architectural judgment.

---

## How to reuse

1. **Copy a block** literally, or merge sections across prompts.
2. **Substitute placeholders** consistently (same names throughout a session).
3. **Add non-negotiables** upfront: Kotlin version, minSdk, Compose BOM, DI choice, serialization rules.
4. **Constrain scope**: forbid drive-by refactors, require matching existing style, cite files before large edits.

### Placeholders

| Token | Meaning |
|--------|---------|
| `{{PROJECT_NAME}}` | Human-readable product name |
| `{{PRIMARY_API}}` | Base URL / API family |
| `{{PACKAGE_ROOT}}` | Application ID package (e.g. `com.example.app`) |
| `{{NETWORK_CLIENT}}` | e.g. Ktor `HttpClient`, Retrofit interface |
| `{{DI_FRAMEWORK}}` | e.g. Koin modules, Hilt |
| `{{NAV_APPROACH}}` | e.g. Navigation Compose typed routes |

---

## 1. Architecture and module layout (single module)

Use when bootstrapping or realigning structure without exploding into multi-module prematurely.

```
You are assisting a Lead Android Engineer. {{PROJECT_NAME}} is a production-quality 
Jetpack Compose app (single `app` module for now—not multi-module unless I ask).

Establish a clean layering mental model compatible with scalability later:

- `core/` — theme, navigation host wiring, DI registration, shared UI primitives; optional
  **`NetworkMonitor`** (connectivity abstraction; Android impl uses `ConnectivityManager`)
- `core/ui/shell/` — app chrome ONLY: top bar, bottom nav, scaffold insets; optional **full-screen
  offline gate** when `!isOnline` (no chrome / NavHost underneath until back online)
- `data/` — DTOs aligned with {{PRIMARY_API}}, remote client built with {{NETWORK_CLIENT}},
  repository implementation; map DTO → domain explicitly
- `domain/` — domain models (non-nullable where the product requires it), repository ports,
  use cases (thin; orchestration only—no Android types)
- `presentation/` — one screen folder per flow: Compose UI + ViewModel + single sealed/ data
  UiState modeling UDF (user events → reducer-style updates → state)

Requirements:
- MVVM + Unidirectional Data Flow: ViewModel exposes one UiState stream; Compose reads state,
  emits intents/events only.
- No business rules in composables beyond trivial UI derivation.
- Prefer constructor injection via {{DI_FRAMEWORK}}; modules grouped by layer.
- Naming and package conventions must stay consistent across the codebase.

Deliver: folder/package plan, naming rules, and a short diagram description (mermaid-friendly)
matching how Activity → Shell → NavHost → screens should depend on each other.
Do not refactor unrelated files; propose minimal diffs.
```

---

## 2. App shell: chrome vs feature routes

Use when separating “global chrome” from product screens (PLP vs PDP hide chrome, tabs, etc.).

```
Lead Android Compose task: introduce an AppShell (`AppScaffold` or equivalent) that OWNS:

- Material3 Scaffold (or equivalent) consuming WindowInsets correctly
- Top app bar variants (standard title/actions)
- Bottom navigation for top-level destinations
- A single injected `NavHostController` passed into the navigation graph composition function

Hard rules:
- Feature screens MUST NOT duplicate system bar handling or reinvent top/bottom bars for
  global chrome—they render content only unless a screen truly needs full custom chrome.
- Shell visibility is ROUTE-driven: e.g. show chrome on tab roots, hide on fullscreen flows
  (modal product details, auth, onboarding)—implement via a small ViewModel or derived state,
  not ad-hoc boolean drilling through every composable without need.
- Placeholder tabs remain minimal—no fake networking.
- If the product includes **search** or other real tabs, wire them in the same `NavHost`; keep
  placeholders only where explicitly out of scope.

Output: scaffold composable skeleton, navigation contract (which routes hide chrome),
and wiring from `MainActivity`. Match existing imports and theme usage in this repo unless
greenfield—in which case scaffold from Material3 defaults first.
```

---

## 2b. App-wide offline (full screen, not per-feature)

Use when “no network” must block the **entire** app (including over PDP), not a snackbar on one tab.

```
Lead Android task: app-wide connectivity gate at the shell root.

Requirements:
- Introduce `NetworkMonitor` exposing `StateFlow<Boolean>` (or equivalent) for **isOnline**.
- Android implementation: `ConnectivityManager.registerDefaultNetworkCallback`; treat loss of a
  usable default network as offline; document whether you require NET_CAPABILITY_VALIDATED vs
  INTERNET-only (trade-off: stricter vs emulator flakiness).
- `AppScaffold` (or root below `setContent`): `Box(fillMaxSize())` — when offline, show a single
  **OfflineFullScreen** (title, body, Retry calling `monitor.refresh()` / re-query capabilities).
  Do NOT render `Scaffold` top/bottom bar or `NavHost` under the offline UI.
- When online returns, **do not** clear the nav back stack by default.
- **Koin**: `single<NetworkMonitor> { AndroidNetworkMonitor(androidContext()) }`; tests/androidTest
  bind a **FakeNetworkMonitor** (toggling `isOnline`) so JVM/instrumentation stay deterministic.
- Manifest: add `ACCESS_NETWORK_STATE` if you read active network capabilities.

Per-screen ViewModels should keep **generic** HTTP/parse errors + Retry; do not duplicate long
“no internet” copy on Search/PLP unless you explicitly want a fallback when monitor and transport disagree.
```

---

## 2c. Product search (API-backed, same grid as PLP)

Use when adding a **Search** tab or route that queries a search endpoint and reuses catalogue tiles.

```
Feature: product search (Lead Android, MVVM + UDF).

API:
- Add client method: GET `{{PRIMARY_API}}/products/search` (or your backend equivalent) with
  `q`, `limit`, `skip`, optional `select` to shrink payloads.
- Repository: `searchProducts(query, limit, skip): Result<ProductsPage>` reusing the same domain
  row type as PLP (`ProductSummary` or equivalent).
- Use case: thin wrapper calling repository.

Presentation:
- `SearchViewModel` + sealed `SearchUiState`: Idle (no request yet), Loading, Success (items,
  favourites, append flag, endReached, **activeQuery** for empty-state copy), Error (message +
  activeQuery + Retry re-runs last query).
- Debounced query (~300–400 ms) after typing stops; IME Search action submits immediately; clear
  resets to Idle without calling the API for blank/whitespace-only input.
- Pagination: mirror PLP (increment `skip` by accumulated item count until `items.size >= total`).
- **DRY**: extract `ProductResultsGrid` (LazyVerticalGrid + scroll threshold + append indicator)
  shared by PLP and Search; keep column-count math in one place (`gridColumnCount` helper).

Navigation:
- Search tab composable receives `onProductClick(id)` → same PDP route as PLP.

Tests:
- MockEngine asserts path, `q`, and `select` for search.
- ViewModel tests: idle, debounced success, empty results, pagination, error + retry, stale
  generation when query changes mid-flight.
```

---

## 3. Navigation: PDP as overlay while keeping PLP alive

Use for “sheet/dialog style” PDP with predictability for back stack and transitions.

```
Navigation Compose expert task (Lead Android mindset):

Product list stays in the nav back stack visually “underneath” product details:

- Implement PDP route as a `dialog` destination (fullscreen; `usePlatformDefaultWidth = false`),
  NOT as a replacement that removes the PLP composition when open (goal: perceptual overlay).
- Pass stable arguments (product id); avoid passing large payloads in routes—fetch by id.

Constraints:
- Back must pop the dialog route predictably (system back and toolbar back if present).
- No duplicate repositories in UI; PDP uses ViewModel + use case as PLP does.
- Predictive Back and gesture dismiss (if required) integrate without breaking nested scroll:

Document edge cases YOU will verify: pager inside PDP, predictive back simultaneous with swipe.

Before coding: list navigation graph deltas and rationale in 5 bullets.
```

---

## 4. Motion: transitions, predictive back, swipe-to-dismiss

Use when US5-like motion is requested without sacrificing scroll correctness.

```
You are collaborating with a Lead Android Engineer implementing rich motion on PDP.

Goals:
- Entry: PDP content animates FROM bottom INTO place; underlying PLP can dim via scrim tied to the
  SAME transition controller (avoid desynchronized fades).
- Exit: symmetric—slide PDP down/off while scrim restores.
- Predictive Back: gesture progress SCRUBS the dismissal animation (`PredictiveBackHandler`);
  interpolate translation + scrim alpha from BACK_EVENT progress.
- Swipe-down-to-dismiss ONLY when PDP’s primary scroll container reports offset == 0 to avoid stealing
  scroll gestures.

Implementation constraints:
- Prefer `Animatable` or `Transition`-compatible state you can scrub; avoid duplicated animation
  sources of truth.
- Keep physics readable; expose constants for durations/offsets near the PDP root composable.
- Do not regress keyboard, IME padding, or talkback focus order—call out semantics if risky.

Produce: phased plan (foundation → gestures → predictive back polish), then code changes scoped
primarily to the PDP composable subtree.
```

---

## 5. Data layer resilience (DTOs vs crash bugs)

Use when aligning models with flaky JSON or avoiding JVM test crashes.

```
Senior Android engineer review: {{PRIMARY_API}} responses are inconsistent; treat network types
as DTOs with nullable fields reflecting the wire—not as strict domain replicas.

Requirements:
- Map DTO → domain in one place (`toDomain()` extensions or mappers); domain enforces what the UX
  needs (defaults, omissions as explicit states).
- Repository returns `Result` or sealed outcomes; ViewModels translate to UiState loading/error/content.
- **No `android.util.Log` or other Android SDK-only APIs inside classes unit-tested on the JVM.**

If proposing logging, gate behind `debugBuild` wrappers or Timber—prefer structured error states.

Keep changes narrowly scoped—do not widen to unrelated IO refactors without approval.
```

---

## 6. Testing strategy (coroutines + ViewModels + instrumentation)

```
Lead QA-minded Android engineer: define a pragmatic pyramid for {{PROJECT_NAME}}.

Unit (JVM):
- ViewModels with `kotlinx-coroutines-test` (`StandardTestDispatcher`, `advanceUntilIdle`).
- Pure use cases and repository implementations using a fake/mock {{NETWORK_CLIENT}} engine
  (no real network).

androidTest where behavior is integration/regression-heavy:
- Custom `Application` + test runner swaps {{DI_FRAMEWORK}} graph to fakes (deterministic fixtures).
- Include **NetworkMonitor** fake (e.g. always online) so shell offline UI does not block unrelated
  tests unless a case explicitly toggles offline.
- One regression suite for known bugs/regressions with semantics-based selectors (prefer test tags
  or unified semantics wrappers over brittle strings).

Contrast / a11y checks:
- Prefer attaching semantics for verification on a root composable over scraping theme colors.

List what NOT to automate yet (golden screenshots everywhere, flaky network)—keep it credible.
```

---

## 7. Branding surfaces (launcher, toolbar)—without breaking adaptive icons

```
Staff Android UX-build task:

- Toolbar: scalable brand asset (prefer vector or suitably padded raster); respect light/dark
  contrast—provide night variant OR tint strategy if monochrome wordmark washes out.
- Launcher: obey adaptive icon SAFE ZONE (~66% centered); foreground must survive circular AND
  squircle masks—inset drawable or regenerate asset with deliberate padding.

Explicitly WARN if a full banner wordmark is unsuitable as adaptive foreground; propose a stacked
glyph or simplified monogram variant when needed.

Do not claim launcher preview from XML alone resolves OEM themed-icon quirks—describe verification
steps on a physical device after reinstall/cache clear.
```

---

## 8. Session discipline (paste at start of heavy refactors)

```
Operating mode: Principal Android Engineer assistant.

Ground rules:
- Minimal diff principle: every changed line earns its place tied to stated goal X.
- Read neighboring files before editing; match Kotlin style and existing abstractions.
- No unsolicited README/documentation unless requested.
- Propose file-level sketch before rewriting > ~100 lines single shot.
- If uncertain about product behavior: ask ONE clarifying multiple-choice—not a questionnaire.

Today's goal: {{ONE_SENTENCE_GOAL}}

Acceptance checks I will apply: {{BULLETED_CHECKS}}
```

---

## Closing note

These prompts prioritize **constraints a staff engineer would articulate**: clear boundaries, JVM-safe boundaries for tests, route-driven chrome, scrubbable motion, **search + pagination** aligned with list endpoints, **shell-level offline** vs per-screen errors, and honest limits of bitmap-based launcher art. Tune tone and tooling names per team; swap `{{.*}}` sections for your next app without importing this project’s incidental API specifics unless you intend to.
