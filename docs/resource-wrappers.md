# Resource & CacheableResource Data Wrappers

This document describes the reactive data state wrappers [`Resource<T>`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/resource/Resource.kt) and [`CacheableResource<T>`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/resource/CacheableResource.kt), alongside their pre-built UI components in [`ResourceUI.kt`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/ui/components/resource/ResourceUI.kt) and [`CacheableResourceUI.kt`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/ui/components/resource/CacheableResourceUI.kt). These utilities bridge domain-level results ([Arrow `Either`](https://arrow-kt.io/learn/typed-errors/either-and-ior/) and [`Ior`](https://arrow-kt.io/learn/typed-errors/either-and-ior/)) into presentation-ready `StateFlow` streams for Jetpack / Compose Multiplatform.

---

## 1. Overview & Purpose

In modern Kotlin Multiplatform applications, UI screens require a standardized way to represent the asynchronous lifecycle of data:
1. **Loading State**: Displaying shimmer or progress spinner placeholders.
2. **Success / Content State**: Rendering data when ready.
3. **Fatal Error State**: Rendering fullscreen error layouts with retry options.
4. **Offline-First / Cached State**: Rendering cached local data while surfacing non-blocking notifications for background sync issues.

Instead of manually writing boolean flags (`isLoading`, `hasError`, `data`) and `when` branches across every screen, the project provides:
- **State Wrappers**: `Resource<T>` (for binary `Either` results) and `CacheableResource<T>` (for caching `Ior` results).
- **Ready-to-Use UI Components**: `ResourceContent`, `CrossfadeResourceContent`, `CacheableResourceContent`, and `CacheableResourceListContent`.

---

## 2. Resource<T>

### Definition
[`Resource<T>`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/resource/Resource.kt) represents a 3-state request lifecycle:

```kotlin
sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<out T>(val value: T) : Resource<T>
    data class Failed(val error: UiError) : Resource<Nothing>
}
```

### When to Use
- Direct remote queries without local database persistence (e.g., search suggestions, profile details, one-way network queries).
- Screens or widgets where cached data is not applicable and fresh data is required.
- Operations where errors are fatal to the display of the current screen.

### Arrow `Either` Integration
`Resource<T>` converts from Arrow `Either<Failure, T>`:

- `Either.Left(Failure)` $\rightarrow$ `Resource.Failed(UiError)` (automatically mapped via `toUiError()`).
- `Either.Right(T)` $\rightarrow$ `Resource.Success(T)`.

### Extension Functions & Helpers
```kotlin
// Inspection
fun <T> Resource<T>.isLoading(): Boolean
fun <T> Resource<T>.hasResult(): Boolean
fun <T> Resource<T>.getValueOrNull(): T?
fun <T> Resource<T>.errorOrNull(): UiError?

// Transformations
inline fun <T, V> Resource<T>.map(transform: (T) -> V): Resource<V>

// Flow to StateFlow in CoroutineScope
fun <T> Flow<Either<Failure, T>>.stateInResource(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.Lazily,
    initialValue: Resource<T> = Resource.Loading,
    onRetry: (() -> Unit)? = null,
    localHandler: ((Failure) -> UiError?)? = null
): StateFlow<Resource<T>>
```

### Ready-to-Use UI Components ([`ResourceUI.kt`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/ui/components/resource/ResourceUI.kt))
- **`ResourceContent`**: Handles `Loading`, `Success`, and `Failed` branches automatically.
- **`CrossfadeResourceContent`**: Adds smooth animated crossfade transitions between loading, success, and error states.

### Usage Example

#### ViewModel:
```kotlin
class UserProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val profile: StateFlow<Resource<UserProfile>> = userRepository
        .observeProfile()
        .stateInResource(viewModelScope)
}
```

#### Compose UI with `ResourceContent`:
```kotlin
@Composable
fun UserProfileScreen(viewModel: UserProfileViewModel = koinInject()) {
    val profileResource by viewModel.profile.collectAsStateWithLifecycle()

    ResourceContent(
        resource = profileResource,
        onLoading = { CircularLoadingBox(Modifier.fillMaxSize()) }, // Default
        onFailed = { error -> DefaultFailedContent(error) },        // Default
        onSuccess = { user ->
            UserProfileDetails(user = user)
        }
    )
}
```

---

## 3. CacheableResource<T>

### Definition
[`CacheableResource<T>`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/resource/CacheableResource.kt) is designed for **offline-first repositories** where data is read from local storage while syncing with a remote backend.

```kotlin
sealed interface CacheableResource<out T> {
    // 1. In-progress initial load
    data object Loading : CacheableResource<Nothing>

    // 2. Fresh data synced from remote server
    data class Newest<out T>(val value: T) : CacheableResource<T>

    // 3. Fatal error with no cached data available (screen is empty)
    data class Failed(val error: UiError) : CacheableResource<Nothing>

    // 4. Offline/Background error: cached data is available to display,
    //    while a non-blocking notification (Snackbar/Dialog) surfaces the sync issue
    data class Cached<out T>(val error: UiError, val value: T) : CacheableResource<T>
}
```

### When to Use
- Offline-first features (e.g., notes list, projects, settings).
- Repositories returning Arrow [`Ior<Failure, T>`](https://arrow-kt.io/learn/typed-errors/either-and-ior/) (Inclusive OR: Left = error only, Right = data only, Both = data + error).
- Screens that must preserve and render local cache even if the network is down or the server returns an error.

### Arrow `Ior` Integration
`CacheableResource<T>` maps directly from `Ior<Failure, T>`:

- `Ior.Left(Failure)` $\rightarrow$ `CacheableResource.Failed(UiError)`
- `Ior.Right(T)` $\rightarrow$ `CacheableResource.Newest(T)`
- `Ior.Both(Failure, T)` $\rightarrow$ `CacheableResource.Cached(UiError, T)`

### Extension Functions & Helpers
```kotlin
// Inspection
fun <T> CacheableResource<T>.isLoading(): Boolean
fun <T> CacheableResource<T>.hasResult(): Boolean
fun <T> CacheableResource<T>.getValueOrNull(): T? // Returns value for both Newest and Cached
fun <T> CacheableResource<T>.getErrorOrNull(): Any?

// Transformations (preserves Cached error while transforming value)
inline fun <T, V> CacheableResource<T>.map(transform: (T) -> V): CacheableResource<V>

// Flow to StateFlow in CoroutineScope
fun <T> Flow<Ior<Failure, T>>.stateInCacheableResource(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.Lazily,
    initialValue: CacheableResource<T> = CacheableResource.Loading,
    localHandler: ((Failure) -> UiError?)? = null
): StateFlow<CacheableResource<T>>
```

### Ready-to-Use UI Components ([`CacheableResourceUI.kt`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/ui/components/resource/CacheableResourceUI.kt))
- **`CacheableResourceContent`**: Handles loading, newest data, cached data, fatal fullscreen error, and non-blocking snackbar notifications for cached errors.
- **`CacheableResourceListContent`**: Specialized wrapper for lists/collections providing empty data handling (`emptyDataContent`) and automatic snackbar management.

### Usage Example

#### ViewModel:
File: [`ProjectsViewModel.kt`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/features/projects/ProjectsViewModel.kt)

```kotlin
class ProjectsViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    // Converts Flow<Ior<Failure, List<Project>>> into StateFlow<CacheableResource<List<Project>>>
    val projects: StateFlow<CacheableResource<List<Project>>> = projectRepository
        .observeAll()
        .stateInCacheableResource(viewModelScope)
}
```

#### Compose UI with `CacheableResourceListContent`:
```kotlin
@Composable
fun ProjectsScreen(viewModel: ProjectsViewModel = koinInject()) {
    val projectsResource by viewModel.projects.collectAsStateWithLifecycle()

    CacheableResourceListContent(
        resource = projectsResource,
        dataContent = { projects ->
            ProjectsList(projects = projects)
        },
        emptyDataContent = {
            EmptyProjectsPlaceholder()
        }
    )
}
```

#### Compose UI with `CacheableResourceContent` (Single item / Custom):
```kotlin
@Composable
fun NoteDetailScreen(viewModel: NoteDetailViewModel = koinInject()) {
    val noteResource by viewModel.note.collectAsStateWithLifecycle()

    CacheableResourceContent(
        resource = noteResource,
        onRetry = { viewModel.retryLoad() },
        dataContent = { note ->
            NoteDetailView(note = note)
        }
    )
}
```

---

## 4. Comparison: Resource vs. CacheableResource

| Feature | `Resource<T>` | `CacheableResource<T>` |
| :--- | :--- | :--- |
| **Domain Source** | `Either<Failure, T>` | `Ior<Failure, T>` |
| **States Count** | 3 (`Loading`, `Success`, `Failed`) | 4 (`Loading`, `Newest`, `Failed`, `Cached`) |
| **Offline-First Support** | No (error discards data) | **Yes** (separates fatal `Failed` vs. non-fatal `Cached`) |
| **UI Helper** | [`ResourceContent`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/ui/components/resource/ResourceUI.kt) | [`CacheableResourceContent`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/ui/components/resource/CacheableResourceUI.kt) / [`CacheableResourceListContent`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/ui/components/resource/CacheableResourceUI.kt) |
| **UI Fallback Strategy** | Fullscreen Error | Displays cached data while notifying of sync failure |
| **Standard Use Case** | Ephemeral queries, search, auth | Persisted entities (Notes, Projects, Tasks) |

---

## 5. Best Practices & Anti-patterns

### Best Practices
- ✅ **Use ready UI wrappers**: Prefer `ResourceContent` / `CacheableResourceListContent` instead of writing manual `when` blocks with repetitive boilerplate.
- ✅ **Use `stateInCacheableResource` for offline-first repositories**: Ensure local data remains visible even when background sync encounters errors.
- ✅ **Transform values with `.map { ... }`**: Use the built-in `map` extension to map Domain models to UI presentation models while preserving the loading/error state wrapper.
- ✅ **Expose `StateFlow<Resource<T>>` directly**: Keep the `Resource` wrapper in the exposed `StateFlow` so that Compose UI can consume it via `ResourceContent`.

### Anti-patterns
- ❌ **Flattening `CacheableResource.Cached` into `Resource.Failed`**: Discarding local cached data on sync failure forces users to stare at an empty error screen while offline.
- ❌ **Re-implementing boilerplate error/loading containers**: Writing custom spinners and error banners per screen instead of using `ResourceUI` / `CacheableResourceUI` components.
- ❌ **Unwrapping in ViewModel into loose properties**:
  ```kotlin
  // BAD:
  var data by mutableStateOf<List<Item>?>(null)
  var isLoading by mutableStateOf(false)
  var error by mutableStateOf<String?>(null)
  
  // GOOD:
  val items: StateFlow<CacheableResource<List<Item>>> = repository.observeItems().stateInCacheableResource(viewModelScope)
  ```
- ❌ **Hardcoding `UiError` mapping in UI**: Let `toResource()` / `toCacheableResource()` convert `Failure` to `UiError` automatically using the centralized error handling pipeline.
