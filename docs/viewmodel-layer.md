# ViewModel Layer Architecture & State Modeling

This document defines the architecture, state management patterns, and naming conventions for the **ViewModel Layer** in the Kotlin Multiplatform (KMP) template.

---

## 1. Overview & Architectural Role

The ViewModel serves as the mediator between the **Data Layer** (Repositories, Room DAOs, DataStore, Ktor APIs) and the **UI Layer** (Compose Multiplatform Screens and Components).

```mermaid
graph LR
    subgraph UI Layer [UI Layer (Compose)]
        Screen[Composable Screen]
        Host[AppEventHandlerHost / LoadingHost]
    end

    subgraph VM Layer [ViewModel Layer]
        VM[ViewModel]
        State[(UiState / StateFlow)]
        Delegates[ErrorHandler / RefreshHandler / EventHandler]
    end

    subgraph Data Layer [Data Layer]
        Repo[Repository]
        DAO[(Room DAO)]
        Store[DataStore]
    end

    Screen -->|1. Triggers on<Action>| VM
    VM -->|2. Queries / Mutations| Repo
    Repo -->|3. Reads / Writes| DAO
    Repo -->|4. Reads / Writes| Store
    DAO -.->|5. Emits Flow updates| VM
    VM -->|6. Updates| State
    State -.->|7. collectAsStateWithLifecycle| Screen
    Delegates -.->|8. Single Events / Loader| Host
```

### Core Responsibilities
1. **State Management**: Exposing observable, lifecycle-aware state (`StateFlow` or Compose `State`) to the UI.
2. **Action Handling**: Exposing public `on<Action>` functions responding to user gestures and events.
3. **Cross-Cutting Delegation**: Connecting [`delegates`](docs/viewmodel-delegates.md) (`ErrorHandler`, `LoadingHandler`, `RefreshHandler`, and `EventHandler`).
4. **Navigation & Flow Orchestration**: Triggering platform-independent screen transitions, tab switching, and result passing via [`Router`](docs/navigation.md).
5. **Lifecycle & Scoping**: Surviving configuration changes and managing long-running tasks in `viewModelScope`.

---

## 2. Screen State Modeling Options

The project supports three primary patterns for modeling ViewModel state depending on screen complexity and interactivity requirements.

### Option A: Immutable StateFlow (Recommended Standard)

Best for complex screens combining multiple data streams (filters, pagination, draft models, loading flags).

#### State Declaration:
```kotlin
data class HomeUiState(
    val filter: NoteFilter = NoteFilter(),
    val notes: List<NoteResponse> = emptyList(),
    val pinnedNotes: List<NoteResponse> = emptyList(),
    val otherNotes: List<NoteResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isSearchExpanded: Boolean = false
)
```

#### ViewModel Implementation:
File: [`HomeViewModel.kt`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/features/home/HomeViewModel.kt)

```kotlin
class HomeViewModel(
    private val noteRepository: NoteRepository,
    private val refreshHandler: RefreshHandler,
    private val errorHandler: ErrorHandler
) : ViewModel(), RefreshHandler by refreshHandler, ErrorHandler by errorHandler {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _paginator = MutableStateFlow(noteRepository.findPaged(_uiState.value.filter))
    val paginator: StateFlow<Paginator<NoteResponse>> = _paginator.asStateFlow()

    init {
        _paginator
            .flatMapLatest { it.uiState }
            .bindToRefresh()
            .onEach { pState ->
                val allNotes = (pState as? PaginatorUIState.Content<NoteResponse>)?.items ?: emptyList()
                val isPaginatorLoading = pState is PaginatorUIState.Loading ||
                        (pState as? PaginatorUIState.Content<*>)?.appendState is PageState.Loading

                _uiState.update { current ->
                    current.copy(
                        notes = allNotes,
                        pinnedNotes = allNotes.filter { it.isPinned },
                        otherNotes = allNotes.filter { !it.isPinned },
                        isLoading = isPaginatorLoading
                    )
                }
            }
            .launchIn(viewModelScope)

        loadNotes(initial = true)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(filter = it.filter.copy(searchQuery = query)) }
        loadNotes(initial = true)
    }
}
```

#### Compose Consumption:
```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        HomeSkeleton()
    } else {
        NotesList(notes = uiState.notes)
    }
}
```

---

### Option B: Mutable Compose State (Per-Property `mutableStateOf`)

Best for high-frequency user input (rich text editing, canvas/drawing, interactive forms with numerous inputs) where flow dispatching overhead is unnecessary and ultra-fine-grained recomposition is desired.

In this approach, **each field is declared as an individual `mutableStateOf` property** with a `private set` modifier.

#### ViewModel Implementation:
```kotlin
class NoteEditorFastViewModel : ViewModel() {

    // 1. Individual mutable state fields with private setters
    var title by mutableStateOf(TextFieldValue(""))
        private set

    var content by mutableStateOf(TextFieldValue(""))
        private set

    var projectId by mutableStateOf<ProjectId?>(null)
        private set

    var isPinned by mutableStateOf(false)
        private set

    var hasReminder by mutableStateOf(false)
        private set

    var hasUnsavedChanges by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // 2. Direct granular updates without full-state copy allocations or Flow emissions
    fun onTitleChange(newValue: TextFieldValue) {
        title = newValue
        hasUnsavedChanges = true
    }

    fun onContentChange(newValue: TextFieldValue) {
        content = newValue
        hasUnsavedChanges = true
    }

    fun onTogglePin() {
        isPinned = !isPinned
        hasUnsavedChanges = true
    }

    fun onToggleReminder() {
        hasReminder = !hasReminder
        hasUnsavedChanges = true
    }
}
```

#### Compose Consumption:
```kotlin
@Composable
fun NoteEditorScreen(viewModel: NoteEditorFastViewModel = koinViewModel()) {
    // Reads individual properties directly; only Composable nodes reading that specific property recompose
    BasicTextField(
        value = viewModel.title,
        onValueChange = viewModel::onTitleChange
    )

    IconButton(onClick = viewModel::onTogglePin) {
        Icon(
            painter = painterResource(Res.drawable.ic_pin),
            contentDescription = "Pin",
            tint = if (viewModel.isPinned) MaterialTheme.colorScheme.primary else LocalContentColor.current
        )
    }
}
```


---

### Option C: Direct Resource Streams (`Resource` / `CacheableResource`)

Best for simple data-fetching screens without local draft forms or complex filtering logic.

#### ViewModel Implementation:
File: [`ProjectsViewModel.kt`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/features/projects/ProjectsViewModel.kt)

```kotlin
class ProjectsViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    // Directly exposes a cached/reactive resource stream
    val projects: StateFlow<CacheableResource<List<Project>>> = projectRepository
        .observeAll()
        .stateInCacheableResource(viewModelScope)
}
```

#### Compose Consumption:
```kotlin
@Composable
fun ProjectsScreen(viewModel: ProjectsViewModel = koinInject()) {
    val projectsResource by viewModel.projects.collectAsStateWithLifecycle()

    CacheableResourceListContent(
        resource = projectsResource,
        dataContent = { projects -> ProjectsList(projects) }
    )
}
```

---

## 3. Naming Conventions & Code Style

Standardized naming ensures consistency across all features in the template.

### 3.1. State Classes & Properties
| Item | Pattern | Example |
| :--- | :--- | :--- |
| **State Class** | `<Feature>UiState` | `HomeUiState`, `NoteEditorUiState` |
| **Form / Draft Model** | `<Feature>Draft` | `NoteEditorDraft`, `ProfileDraft` |
| **Public Flow Property** | `uiState` | `val uiState: StateFlow<HomeUiState>` |
| **Direct Entity Flow** | `<pluralEntity>` | `val projects: StateFlow<CacheableResource<List<Project>>>` |
| **Private Mutable Flow** | `_<name>` | `private val _filter = MutableStateFlow(...)` |
| **Boolean Flags** | `is<State>` / `has<State>` | `isLoading`, `isSearchExpanded`, `isPinned`, `hasUnsavedChanges` |
| **List Properties** | `<items>` / `<category><Items>` | `notes`, `pinnedNotes`, `otherNotes` |

### 3.2. Public User Action Methods
All functions invoked from Compose UI must use the `on<Action>` prefix describing **what user action occurred**, rather than internal implementation details:

| Action Category | Function Signature | Description |
| :--- | :--- | :--- |
| **Button / Item Click** | `fun onSaveClick(onSuccess: () -> Unit)`<br>`fun onNoteClick(noteId: NoteId)`<br>`fun onCreateNoteClick()` | User clicked a button or list item |
| **Input / Form Change** | `fun onTitleChange(newValue: TextFieldValue)`<br>`fun onSearchQueryChange(query: String)` | User modified input or text field |
| **Toggle / Switch** | `fun onTogglePin()`<br>`fun onToggleReminder()`<br>`fun onSearchExpandToggle(expanded: Boolean)` | User toggled a boolean switch or UI panel |
| **Undo / Redo / Focus** | `fun onUndo()`<br>`fun onRedo()`<br>`fun onFocusLost()` | Local editor actions and lifecycle |
| **Pull-to-Refresh & Paging** | `fun onRefresh()`<br>`fun onRetry()`<br>`fun onLoadNextPage()` | Pull-to-refresh, retry, and infinite scroll |
| **Navigation & Modals** | `fun onBack()`<br>`fun onDismiss()`<br>`fun onSelectContextClick()` | Back press, modal dismissal, picker navigation |

### 3.3. Private / Internal Helper Methods
Private methods inside the ViewModel use imperative verbs describing the operation:
- `load<Entity>(...)`: e.g. `loadNotes(initial = true)`, `loadNote(id)`
- `sync<Entity>(...)`: e.g. `syncDraft()`
- `apply<Transformation>(...)`: e.g. `applyFilter()`

---

## 4. Form Validation Framework (Validator DSL)

Located in: [`com.hackathon.finni.core.presentation.validator`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/presentation/validator/Validator.kt)

For user input validation (forms, note editing, authentication, settings), the project provides a lightweight, declarative validation DSL based on [`compositeValidator`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/presentation/validator/Validator.kt). It supports combining multiple field validators, multi-condition checks on single fields, customizable evaluation operators, and granular observation of validation results for UI error states.

### 4.1. Core Components

| Component | Interface / Class | Responsibility |
| :--- | :--- | :--- |
| **`Validator`** | `interface Validator` | Base interface exposing `fun validate(): Boolean`. |
| **`ValueValidator<T>`** | `class ValueValidator<T>` | Validates a single value supplier against a list of conditions using an `Operator`. |
| **`CompositeValidator`** | `class CompositeValidator` | Groups multiple validators and provides `onValid { ... }` execution callback. |
| **`Condition<T>`** | `fun interface Condition<T>` | Single predicate `(T) -> Boolean` with `.observable(...)` decorator. |
| **`Operator<T>`** | `fun interface Operator<T>` | Strategy for executing and combining condition/validator checks. |

---

### 4.2. Evaluation Operators (`Operator<T>`)

Located in: [`Operator.kt`](file:///E:/Dev/AndroidStudioProjects/Finni/shared/src/commonMain/kotlin/com/hackathon/finni/core/presentation/validator/Operator.kt)

Operators define whether checks should short-circuit or execute completely:

- **`Operator.allEach()` (Default)**: Full execution `AND`. Evaluates **all** conditions/validators even if one fails. **Crucial for forms** so all invalid fields and rules fire their `onResult` error callbacks simultaneously.
- **`Operator.all()`**: Short-circuit `AND`. Stops immediately at the first failing condition.
- **`Operator.anyEach()`**: Full execution `OR`. Runs all checks; returns `true` if at least one passes.
- **`Operator.any()`**: Short-circuit `OR`. Stops immediately at the first passing condition.

```kotlin
// Setting an operator at the composite (form) level:
val validator = compositeValidator(operator = Operator.allEach()) { ... }

// Setting an operator for a specific field:
value(this::password, operator = Operator.allEach()) { ... }
```

---

### 4.3. Multi-Condition Field Checks & Observation

A single field can have multiple independent conditions with field-level or condition-level observation callbacks:

```kotlin
class SignUpViewModel : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")

    // Observation states for UI feedback
    var emailError by mutableStateOf<String?>(null)
    var passwordError by mutableStateOf<String?>(null)
    var hasMinLength by mutableStateOf(false)
    var hasDigit by mutableStateOf(false)
    var hasSpecialChar by mutableStateOf(false)

    val formValidator = compositeValidator {
        // 1. Single-condition field with direct error message mapping
        value(this@SignUpViewModel::email) {
            onResult { isValid ->
                emailError = if (isValid) null else "Enter a valid email address"
            }
            check({ it.isNotBlank() && it.isEmail() })
        }

        // 2. Multi-condition field with granular checklist observation
        value(this@SignUpViewModel::password) {
            onResult { isValid ->
                passwordError = if (isValid) null else "Password does not meet requirements"
            }
            check({ it.length >= 8 }) { hasMinLength = it }
            check({ it.any { c -> c.isDigit() } }) { hasDigit = it }
            check({ it.any { c -> !c.isLetterOrDigit() } }) { hasSpecialChar = it }
        }
    }

    fun onSignUpClick() {
        formValidator.onValid {
            // Executed ONLY if all fields and conditions pass
            submitRegistration()
        }
    }
}
```

---

### 4.4. Triggering Validation & Compose UI Integration

The `CompositeValidator` exposes two invocation styles:

1. **`validator.validate(): Boolean`**: Executes all rules, triggers `onResult` callbacks, and returns the aggregate `Boolean` status.
2. **`validator.onValid { ... }`**: Validates the form and executes the trailing lambda only when valid.

#### Compose UI Binding:
```kotlin
@Composable
fun SignUpScreen(viewModel: SignUpViewModel = koinViewModel()) {
    Column {
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            isError = viewModel.emailError != null,
            supportingText = { viewModel.emailError?.let { Text(it) } }
        )

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            isError = viewModel.passwordError != null,
            supportingText = { viewModel.passwordError?.let { Text(it) } }
        )

        // Dynamic visual checklist driven by condition-level callbacks
        PasswordRequirementItem("At least 8 characters", isMet = viewModel.hasMinLength)
        PasswordRequirementItem("Contains a number", isMet = viewModel.hasDigit)
        PasswordRequirementItem("Contains a special character", isMet = viewModel.hasSpecialChar)

        Button(onClick = viewModel::onSignUpClick) {
            Text("Create Account")
        }
    }
}
```

---

## 5. Interconnection Between Layers

```text
┌───────────────────────────────────────────────────────────────────┐
│                           UI Layer                                │
│  - Collects uiState with collectAsStateWithLifecycle()            │
│  - Calls viewModel.on<Action>() on user events                    │
│  - Hosts AppEventHandlerHost & LoadingHost for global side-effects│
└─────────────────────────────────▲─────────────────────────────────┘
                                  │ (StateFlow / User Actions)
┌─────────────────────────────────▼─────────────────────────────────┐
│                        ViewModel Layer                            │
│  - Produces immutable UiState via combine / stateIn               │
│  - Validates forms via compositeValidator                         │
│  - Triggers screen navigation & receives results via Router       │
│  - Uses ErrorHandler.launchSafe to execute mutations              │
│  - Uses RefreshHandler.bindToRefresh for list streams             │
│  - Dispatches UIEvent (Toast, Dialog, Snackbar) via EventHandler  │
└─────────────────────────────────▲─────────────────────────────────┘
                                  │ (Repository calls & Flow streams)
┌─────────────────────────────────▼─────────────────────────────────┐
│                           Data Layer                              │
│  - Room DAOs emit reactive Flow<Entity> / Flow<List<Entity>>      │
│  - NetworkBoundResource emits Flow<Ior<Failure, T>>               │
│  - Repositories return RequestResult<T> (Either<Failure, T>)      │
└───────────────────────────────────────────────────────────────────┘
```

---

## 6. Comparison: State Modeling Approaches

| Feature | Option A: Immutable `StateFlow` | Option B: Per-Property `mutableStateOf` | Option C: Direct `Resource` |
| :--- | :--- | :--- | :--- |
| **Mechanism** | `StateFlow<UiState>` via `combine` | Individual `var <prop> by mutableStateOf` | `StateFlow<CacheableResource<T>>` |
| **Primary Use Case** | Complex multi-stream screens | High-frequency input forms/editors | Simple CRUD / Read-only views |
| **Thread Safety** | Thread-safe across coroutines | Compose Snapshot thread-bound | Thread-safe |
| **Recomposition Scope** | Recomposes entire collector node | Ultra-fine-grained (only nodes reading the property) | Handled by `ResourceContent` UI |
| **Testing** | Standard Turbine / Coroutine test | Compose test rule / Snapshot | Standard Turbine test |

---

## 7. Best Practices & Anti-patterns

### Best Practices
- ✅ **Use `SharingStarted.WhileSubscribed(5000)`**: Keeps upstream coroutines active for 5 seconds during configuration changes (screen rotations, desktop resizing) to prevent duplicate queries.
- ✅ **Wrap mutations in `ErrorHandler.launchSafe`**: Never write raw `viewModelScope.launch` with manual `try-catch` for user actions.
- ✅ **Use `compositeValidator` with `Operator.allEach()`**: Ensures all form error states update simultaneously when the user submits invalid input.
- ✅ **Keep `UiState` pure and data-only**: Include only data models, primitives, and `UiText` in `UiState`.
- ✅ **Expose read-only `StateFlow`**: Keep `_uiState = MutableStateFlow` private and expose `val uiState: StateFlow`.

### Anti-patterns
- ❌ **Passing Android Context or Compose Modifiers into ViewModel**: ViewModels must remain 100% platform-agnostic in `commonMain`.
- ❌ **Storing one-time events in `UiState`**: Do not put `val showToast: Boolean` in `UiState`; dispatch via `EventHandler.sendEvent(UIEvent.Toast(...))` instead.
- ❌ **Naming action functions with technical details**: Name methods by user intent (`onSaveClick`, `onTitleChange`), not technical steps (`sendSaveHttpRequest`, `updateDraftInDb`).
- ❌ **Mutable collections in `UiState`**: Never use `ArrayList` or `MutableList` in `UiState`; always use `List<T>` or `Map<K, V>`.

