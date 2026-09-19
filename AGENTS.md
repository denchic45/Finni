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
- **ViewModel Architecture & State**: [docs/viewmodel-layer.md](docs/viewmodel-layer.md) — ViewModel architecture, state modeling options (Immutable StateFlow vs. Compose MutableState), naming conventions, and layer connections.
- **ViewModel Delegates & State Handlers**: [docs/viewmodel-delegates.md](docs/viewmodel-delegates.md) — ViewModel delegate composition pattern (`ErrorHandler`, `RefreshHandler`, `LoadingHandler`, `EventHandler`).
- **Navigation & Router**: [docs/navigation.md](docs/navigation.md) — Navigation 3 architecture, `Router`, backstack transformations, `Destination` hierarchy, async result passing, and `AppNavDisplay`.
- **Resource & Data Wrappers**: [docs/resource-wrappers.md](docs/resource-wrappers.md) — Reactive data state wrappers (`Resource<T>` and `CacheableResource<T>`) bridging Arrow `Either`/`Ior` with UI states.
- **Resources & UI Models**: [docs/resources.md](docs/resources.md) — Compose Multiplatform resources (`composeResources`), naming conventions, `UiText`/`UiImage` presentation models, and anti-patterns.
- **Data Layer Architecture**: [docs/data-layer.md](docs/data-layer.md) — Offline-first architecture, Room DAOs, DataStore, NetworkBoundResource, Paginator, and OfflineEntityHandler sync.

## Guidelines for AI Agents
1. **Prefer Shared Logic**: Always try to implement features in `shared/src/commonMain` first. Avoid platform-specific code unless necessary.
2. **Follow Existing Patterns**: Look at existing ViewModels (e.g., `NoteEditorViewModel.kt`, `HomeViewModel.kt`, `ProjectsViewModel.kt`) and UI components before creating new ones.
3. **Follow ViewModel Conventions**: Follow [docs/viewmodel-layer.md](docs/viewmodel-layer.md) for state modeling (`UiState` with `StateFlow`), `on<Action>` naming, and layer interconnections.
4. **Use ViewModel Delegates**: Follow [docs/viewmodel-delegates.md](docs/viewmodel-delegates.md) for handling errors, loading states, one-time events, and pull-to-refresh.
5. **Use Router for Navigation**: Follow [docs/navigation.md](docs/navigation.md) for backstack operations (`push`, `pop`, `switchTab`), `Destination` hierarchy, and result passing (`sendResult`, `receiveResult`).
6. **Use Resource Data Wrappers**: Follow [docs/resource-wrappers.md](docs/resource-wrappers.md) for async request states (`Resource<T>`) and offline-first cached streams (`CacheableResource<T>`).
7. **Follow Resource & UI Conventions**: Follow [docs/resources.md](docs/resources.md) for `composeResources`, `UiText`, `UiImage`, and icon/string naming. Avoid `material-icons-extended`.
8. **Follow Data Layer Architecture**: Follow [docs/data-layer.md](docs/data-layer.md) for Room DAOs, `NetworkBoundResource` caching (`observeData`), `Paginator`, and `OfflineEntityHandler` background sync.
9. **Dependency Injection**: Check how dependencies are provided in `CoreModule.kt` and feature modules (Koin).
10. **Documentation**: Keep `docs/` updated for complex architectural decisions.
11. **Consistency**: Maintain the established coding style and naming conventions.
