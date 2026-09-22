# Agent Guidelines

Welcome to the **Finni** project! This is a Kotlin Multiplatform (KMP) project targeting Android, Desktop, and iOS.

## Project Structure
- `shared/`: Contains the core business logic, ViewModels, and data layers shared across all platforms.
- `androidApp/`: Android-specific implementation and entry point.
- `desktopApp/`: Desktop-specific implementation and entry point.
- `iosApp/`: iOS-specific implementation (Swift/Xcode).
- `common/`: Likely shared logic or utilities used by multiple modules.

## Tech Stack
- **Language**: Kotlin (Common, JVM, Native)
- **UI**: Compose Multiplatform (for Android, Desktop, and iOS)
- **Architecture**: MVVM (Model-View-ViewModel) with logic primarily in `shared`.
- **Navigation**: Jetpack Navigation 3 / Navigation Compose (`androidx.navigation3`).
- **Image Loading**: Coil 3 (`coil3`, `coil3.compose`).
- **Concurrency**: Kotlin Coroutines & Flow.
- **Dependency Injection**: Koin (`koin-core`, `koin-compose`).
- **Database**: Room Multiplatform (`androidx.room`) with SQLite DAOs.
- **Key-Value / Storage**: AndroidX DataStore (`PreferencesDataStore` for app settings, typed `DataStore<AuthSettings>` for auth tokens).



## Documentation Index

### 1. Game Design & Product Specs (`docs/game-design/`)

- **Game Design & Mechanics Spec**: [docs/game-design/AGENTS.md](docs/game-design/AGENTS.md) — Core
  principles, ethical guardrails, 3-phase daily loop, pet metrics (Hunger, Mood, TimeOfDay), and
  crisis management.
- **Technical Specification (ТЗ)**: [docs/game-design/TZ.md](docs/game-design/TZ.md) — Full
  competition technical requirements, age-appropriate guidelines, and demo mode.
- **Story Chapters & Economy**: [docs/game-design/levels.md](docs/game-design/levels.md) — 6 story
  chapters, 13 tasks breakdown, locations, and coin balance math.
- **Task Templates & Mechanics**: [docs/game-design/tasks.md](docs/game-design/tasks.md) — 4
  reusable Compose task templates (`DILEMMA`, `SMART_SHOP`, `CARD_SORTING`, `BUDGET_SPLIT`).

### 2. Core Architecture & Patterns (`docs/core/`)

- **ViewModel Architecture & State**: [docs/core/viewmodel-layer.md](docs/core/viewmodel-layer.md) —
  ViewModel architecture, state modeling options (Immutable StateFlow vs. Compose MutableState),
  naming conventions, and layer connections.
- **ViewModel Delegates & State Handlers
  **: [docs/core/viewmodel-delegates.md](docs/core/viewmodel-delegates.md) — ViewModel delegate
  composition pattern (`ErrorHandler`, `RefreshHandler`, `LoadingHandler`, `EventHandler`).
- **Navigation & Router**: [docs/core/navigation.md](docs/core/navigation.md) — Navigation 3
  architecture, `Router`, backstack transformations, `Destination` hierarchy, async result passing,
  and `AppNavDisplay`.
- **Resource & Data Wrappers**: [docs/core/resource-wrappers.md](docs/core/resource-wrappers.md) —
  Reactive data state wrappers (`Resource<T>` and `CacheableResource<T>`) bridging Arrow `Either`/
  `Ior` with UI states.
- **Resources & UI Models**: [docs/core/resources.md](docs/core/resources.md) — Compose
  Multiplatform resources (`composeResources`), naming conventions, `UiText`/`UiImage` presentation
  models, and anti-patterns.
- **Data Layer Architecture**: [docs/core/data-layer.md](docs/core/data-layer.md) — Offline-first
  architecture, Room DAOs, DataStore, NetworkBoundResource, Paginator, and OfflineEntityHandler
  sync.

### 3. Implementation Specs (`docs/implementation/`)

- *Feature specifications, screen models, database entities, and feature-level architecture (in
  progress).*

## Guidelines for AI Agents
1. **Prefer Shared Logic**: Always try to implement features in `shared/src/commonMain` first. Avoid platform-specific code unless necessary.
2. **Follow Existing Patterns**: Look at existing ViewModels (e.g., `NoteEditorViewModel.kt`, `HomeViewModel.kt`, `ProjectsViewModel.kt`) and UI components before creating new ones.
3. **Follow Game Design Specifications**: Refer
   to [docs/game-design/AGENTS.md](docs/game-design/AGENTS.md), [docs/game-design/levels.md](docs/game-design/levels.md),
   and [docs/game-design/tasks.md](docs/game-design/tasks.md) for game rules, pedagogical
   guardrails, and task templates.
4. **Follow ViewModel Conventions**:
   Follow [docs/core/viewmodel-layer.md](docs/core/viewmodel-layer.md) for state modeling (`UiState`
   with `StateFlow`), `on<Action>` naming, and layer interconnections.
5. **Use ViewModel Delegates**:
   Follow [docs/core/viewmodel-delegates.md](docs/core/viewmodel-delegates.md) for handling errors,
   loading states, one-time events, and pull-to-refresh.
6. **Use Router for Navigation**: Follow [docs/core/navigation.md](docs/core/navigation.md) for
   backstack operations (`push`, `pop`, `switchTab`), `Destination` hierarchy, and result passing (
   `sendResult`, `receiveResult`).
7. **Use Resource Data Wrappers**:
   Follow [docs/core/resource-wrappers.md](docs/core/resource-wrappers.md) for async request
   states (`Resource<T>`) and offline-first cached streams (`CacheableResource<T>`).
8. **Follow Resource & UI Conventions**: Follow [docs/core/resources.md](docs/core/resources.md) for
   `composeResources`, `UiText`, `UiImage`, and icon/string naming. Avoid `material-icons-extended`.
9. **Follow Data Layer Architecture**: Follow [docs/core/data-layer.md](docs/core/data-layer.md) for
   Room DAOs, `NetworkBoundResource` caching (`observeData`), `Paginator`, and
   `OfflineEntityHandler` background sync.
10. **Dependency Injection**: Check how dependencies are provided in `CoreModule.kt` and feature
    modules (Koin).
11. **Documentation**: Keep `docs/` updated for complex architectural decisions.
12. **Consistency**: Maintain the established coding style and naming conventions.
13. **Date & Time Standards**: Always use `kotlin.time.Instant` and `kotlin.time.Clock` from the
    Kotlin standard library. Avoid deprecated `kotlinx.datetime.Instant` and
    `kotlinx.datetime.Clock`. Refer
    to [docs/core/data-layer.md](docs/core/data-layer.md#10-date-time--serialization-standards).
