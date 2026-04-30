# Next Android Technical Challenge (DummyJSON)

This repository is a Jetpack Compose Android app that consumes DummyJSON’s Products API and implements a subset of the challenge user stories and bug fixes.

## Tech stack
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: Clean-ish **MVVM + UDF** (single `UiState` per screen)
- **DI**: **Koin**
- **Networking**: **Ktor** + `kotlinx.serialization`
- **Image loading**: **Coil (compose)**
- **Navigation**: `navigation-compose`
- **Testing**: JUnit + `kotlinx-coroutines-test`

## Architecture overview (high level)
- **`core/`**: DI modules, navigation, theme
- **`data/`**: DTOs + API client + repository implementation
- **`domain/`**: models + repository interface + use cases
- **`presentation/`**: Compose screens + ViewModels + `UiState`

## Implemented features

### User Stories
- **User Story 1 (Priority 1) – List Tile UI**
  - Product tile shows **thumbnail**, **title (max 2 lines)**, **brand**, and **price**.
  - PLP tile supports **SALE** badge (red background, white text), **favourite** toggle, and **golden star rating**.
  - Image fallback uses a simple placeholder when thumbnail is missing.

- **User Story 2 (Priority 3) – Product List Grid Layout**
  - PLP is a **responsive grid** using `LazyVerticalGrid` with a calculated **fixed column count** to guarantee **at least 2 columns** on phones and scale up on larger screens.
  - Uses stable item keys for smooth scrolling.
  - **Infinite scroll pagination** using DummyJSON `limit` + `skip` (page size 30).

- **User Story 4 (Priority 2) – Product Details Page (PDP)**
  - Navigates from PLP → PDP.
  - PDP displays: title, SKU (`id`), **image carousel** with dot indicator, price (sale/original), rating, brand/category, description, warranty/return/shipping info, and a sticky **Add to Cart** CTA.
  - PDP supports **pinch-to-zoom + pan** on images while preserving **pager swiping** when not zoomed.
  - PDP shows **reviews** (name → stars → quoted comment → date formatted as `DD - Month - YYYY`), and tapping the rating near the price scrolls to the reviews section.

### App chrome (home)
- PLP screen includes a top app bar (title + cart icon) and a bottom navigation bar (Home/Search/Saved/Bag/Account). Non-Home tabs are placeholders for now.

### Bug fixes (from the challenge PDF)
- **Bug 1 (Priority 1) – Crash on app load**
  - Replaced strict product model with endpoint-aligned **DTOs** (nullable fields) and mapping to domain models.
  - Added error handling so failures show an error state rather than crashing.

- **Bug 2 (Priority 2) – UI below system bars**
  - Fixed insets/padding usage with `Scaffold` and safe drawing insets.

- **Bug 3 (Priority 3) – Text is magenta**
  - Removed forced magenta `onSurface` override from the theme.

- **Bug 4 (Priority 2) – Requests taking too long**
  - Removed artificial delay from the API client and added loading UI states.

## Tests
- Added **unit tests** for ViewModel state behavior using `kotlinx-coroutines-test`.

Run tests:

```bash
./gradlew testDebugUnitTest
```

## How to run
Open the project in Android Studio and run the `app` configuration, or from CLI:

```bash
./gradlew :app:installDebug
```

## Pending user stories / enhancements
- **App shell refactor (scalability)**
  - Move top app bar + bottom navigation out of `ProductsScreen` into a dedicated app-shell layer, so feature screens only contain feature logic/UI.

- **User Story 3 (Priority 4) – Enhanced Search**
  - Add a search UI, state, and use DummyJSON search endpoint (`/products/search?q=...`).

- **User Story 5 (Priority 3) – PLP ↔ PDP transitions + predictive back**
  - Implement slide/fade transitions and ensure predictive back gesture integration.

- **UI polish**
  - Replace placeholder tab content screens (Search/Saved/Bag/Account) with real features.
  - Consider Window Size Classes for explicit compact/medium/expanded layouts.

## Notes / assumptions
- Currency formatting is currently **Locale.UK** to ensure a currency symbol is shown.
- “SKU” is represented by the DummyJSON `id` field (no separate SKU field in the API payload used).

