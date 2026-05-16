# Audius - Migration to Compose Multiplatform Best Practices

## Executive Summary

This document outlines a comprehensive migration plan to transform the Audius application from a custom navigation/state management architecture to industry-standard **Compose Multiplatform best practices**. The migration includes:

- ✅ Moving from custom `StateManager` to standard **MVVM with Jetpack Compose ViewModel**
- ✅ Replacing custom navigation with **Compose Navigation** (androidx.navigation)
- ✅ Moving UI from Android-only to **shared Compose Multiplatform UI**
- ✅ Removing legacy ViewModels and embracing modern architecture patterns
- ✅ Full **iOS support using Compose Multiplatform**
- ✅ Centralizing state management with **StateFlow/SharedFlow**

---

## Current Architecture Analysis

### Problems with Current Architecture

1. **Custom Navigation System**: Complex custom navigation using `ScreenIdentifier`, `Navigation`, and manual backstack management
   - Not standardized (Compose Navigation is the industry standard)
   - Harder to maintain and test
   - Lost out on ecosystem benefits (deep linking, saved state, etc.)

2. **Custom ViewModel Pattern**: `JustListenViewModel` with manual `StateManager`
   - Not compatible with standard Jetpack ViewModel
   - Limited lifecycle awareness
   - Manual state management across screens
   - No built-in `SavedStateHandle` support

3. **Android-Only Compose UI**: All Composables in `androidApp`, none in `shared`
   - Cannot reuse UI on iOS
   - Duplicate UI logic for iOS (currently just a stub)
   - No multiplatform benefits

4. **Manual State Holder Management**: Using `SaveableStateHolder` manually
   - Complex backstack management logic
   - Hard to test
   - Error-prone state preservation

5. **Spread Event Handling**: `Events` class scattered across codebase
   - No centralized state updates
   - Manual coroutine management
   - Difficult to track state changes

### Current File Structure

```
shared/
├── src/commonMain/
│   ├── datalayer/          # ✅ Good - Keep as is
│   ├── media/              # ✅ Good - Keep as is
│   ├── viewmodel/
│   │   ├── JustListenViewModel.kt  # ❌ Replace with Compose ViewModel
│   │   ├── Events.kt               # ❌ Replace with standard event handlers
│   │   ├── StateProvider.kt        # ⚠️ Refactor
│   │   └── screens/                # ⚠️ Refactor to screen-specific VMs
│   ├── Navigation.kt       # ❌ Remove - Replace with Compose Navigation
│   ├── StateManager.kt     # ❌ Remove - Complex state mgmt not needed
│   ├── ScreenIdentifier.kt # ❌ Remove - Compose Nav Routes handle this
│   └── ui/                 # ✅ UI to be moved here (currently in androidApp)
│
├── src/androidMain/
│   └── viewmodel/          # ✅ Android-specific DI setup
│
└── src/iosMain/           # ⚠️ To be implemented

androidApp/
├── src/main/java/
│   └── ui/                 # ❌ Move to shared/commonMain/ui
│       ├── addplaylistscreen/
│       ├── libraryscreen/
│       ├── playlistscreen/
│       ├── playlistdetailscreen/
│       ├── searchscreen/
│       ├── settingsscreen/
│       ├── MainComposable.kt
│       ├── Router.kt       # ❌ Replace with Compose NavHost
│       └── screenpicker/   # ❌ Remove - handled by NavHost

iosApp/
└── iosApp/                # ⚠️ Currently just a stub
```

---

## Migration Plan

### Phase 0: Preparation & Documentation

**Objectives:**
- Set up feature branches
- Analyze dependencies
- Plan incremental rollout

**Tasks:**
1. Create feature branch: `feature/compose-multiplatform-migration`
2. Document all screen dependencies and inter-screen navigation
3. Create a DSL/route definitions for all screens
4. Set up integration test framework (optional but recommended)
5. Document current state preservation requirements

**Duration:** 1-2 days

---

### Phase 1: Update Dependency Management

**Objectives:**
- Add Compose Navigation and ViewModel dependencies
- Configure Compose Multiplatform properly
- Set up KSP for annotation processing

**Changes to `settings.gradle.kts`:**
```kotlin
// Add Compose Navigation
library("navigation-compose", "androidx.navigation", "navigation-compose").version("2.8.0")

// Add Lifecycle/ViewModel
version("lifecycleVersion", "2.8.0")
library("lifecycle-viewmodel", "androidx.lifecycle", "lifecycle-viewmodel").versionRef("lifecycleVersion")
library("lifecycle-runtime", "androidx.lifecycle", "lifecycle-runtime").versionRef("lifecycleVersion")
library("lifecycle-viewmodel-compose", "androidx.lifecycle", "lifecycle-viewmodel-compose").versionRef("lifecycleVersion")

// Add kotlinx.serialization for route serialization
library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").version("1.7.0")
```

**Changes to `shared/build.gradle.kts`:**
```kotlin
kotlin {
    commonMain {
        dependencies {
            // Navigation Compose (Multiplatform)
            implementation("androidx.navigation:navigation-compose:2.8.0")
            
            // ViewModel + State Management
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.8.0")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime:2.8.0")
            
            // Serialization for routes
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")
        }
    }
    
    androidMain {
        dependencies {
            implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.0")
            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
            implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
        }
    }
}
```

**Duration:** 1 day

---

### Phase 2: Create Shared Compose Navigation & Routes

**Objectives:**
- Define all app routes in a type-safe manner
- Create navigation graph structure
- Implement deep linking infrastructure

**New File: `shared/src/commonMain/kotlin/com/rld/justlisten/navigation/Routes.kt`**

```kotlin
@Serializable
sealed class Route {
    @Serializable
    data object Library : Route()
    
    @Serializable
    data object Playlist : Route()
    
    @Serializable
    data class PlaylistDetail(
        val playlistId: String,
        val playlistIcon: String,
        val playlistTitle: String,
        val playlistCreatedBy: String,
        val playlistEnum: String,
    ) : Route()
    
    @Serializable
    data class AddPlaylist(val initialData: String = "") : Route()
    
    @Serializable
    data object Search : Route()
    
    @Serializable
    data object Settings : Route()
    
    @Serializable
    data object Donation : Route()
}

// Navigation levels for bottom bar
enum class NavigationLevel {
    LEVEL_1,   // Bottom bar tabs
    LEVEL_2,   // Modal/Detail screens
    LEVEL_3    // Nested modals
}

// Associate routes with navigation levels
val Route.navigationLevel: NavigationLevel
    get() = when (this) {
        Route.Library,
        Route.Playlist,
        Route.Search,
        Route.Settings,
        Route.Donation -> NavigationLevel.LEVEL_1
        
        Route.AddPlaylist,
        is Route.PlaylistDetail -> NavigationLevel.LEVEL_2
    }

// Bottom bar destinations (LEVEL_1 only)
val BOTTOM_BAR_ROUTES = listOf(
    Route.Library,
    Route.Playlist,
    Route.Search,
    Route.Settings,
    Route.Donation,
)
```

**New File: `shared/src/commonMain/kotlin/com/rld/justlisten/navigation/AppNavigation.kt`**

```kotlin
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: Route = Route.Library,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn() + slideInHorizontally() },
        exitTransition = { fadeOut() + slideOutHorizontally() },
    ) {
        composable<Route.Library> {
            LibraryScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBackPressed = { navController.popBackStack() },
            )
        }
        
        composable<Route.Playlist> {
            PlaylistScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBackPressed = { navController.popBackStack() },
            )
        }
        
        composable<Route.PlaylistDetail> { backStackEntry ->
            val args: Route.PlaylistDetail = backStackEntry.toRoute()
            PlaylistDetailScreen(
                playlistId = args.playlistId,
                onNavigate = { route -> navController.navigate(route) },
                onBackPressed = { navController.popBackStack() },
            )
        }
        
        composable<Route.Search> {
            SearchScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBackPressed = { navController.popBackStack() },
            )
        }
        
        composable<Route.AddPlaylist> { backStackEntry ->
            val args: Route.AddPlaylist = backStackEntry.toRoute()
            AddPlaylistScreen(
                initialData = args.initialData,
                onNavigate = { route -> navController.navigate(route) },
                onBackPressed = { navController.popBackStack() },
            )
        }
        
        composable<Route.Settings> {
            SettingsScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBackPressed = { navController.popBackStack() },
            )
        }
        
        composable<Route.Donation> {
            DonationScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBackPressed = { navController.popBackStack() },
            )
        }
    }
}
```

**Duration:** 2 days

---

### Phase 3: Refactor to Modern MVVM Architecture

**Objectives:**
- Replace `JustListenViewModel` with screen-specific ViewModels
- Use Jetpack Compose `ViewModel`
- Implement proper state management with StateFlow

**New File: `shared/src/commonMain/kotlin/com/rld/justlisten/viewmodel/BaseScreenViewModel.kt`**

```kotlin
abstract class BaseScreenViewModel(
    protected val repository: Repository,
) : ViewModel() {
    protected val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    protected val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvents: Flow<NavigationEvent> = _navigationEvents.receiveAsFlow()
    
    protected fun navigate(route: Route) {
        viewModelScope.launch {
            _navigationEvents.send(NavigationEvent.NavigateTo(route))
        }
    }
    
    protected fun popBackStack() {
        viewModelScope.launch {
            _navigationEvents.send(NavigationEvent.PopBackStack)
        }
    }
}

sealed class UiState {
    data object Loading : UiState()
    data class Success(val data: Any) : UiState()
    data class Error(val message: String) : UiState()
}

sealed class NavigationEvent {
    data class NavigateTo(val route: Route) : NavigationEvent()
    data object PopBackStack : NavigationEvent()
}
```

**New File: `shared/src/commonMain/kotlin/com/rld/justlisten/viewmodel/library/LibraryViewModel.kt`**

```kotlin
class LibraryViewModel(repository: Repository) : BaseScreenViewModel(repository) {
    private val _libraryState = MutableStateFlow(LibraryScreenState())
    val libraryState: StateFlow<LibraryScreenState> = _libraryState.asStateFlow()
    
    init {
        loadLibraryData()
    }
    
    private fun loadLibraryData() {
        viewModelScope.launch {
            _libraryState.update { it.copy(isLoading = true) }
            try {
                val recentSongs = repository.getRecentSongs(20)
                val favorites = repository.getFavoritePlaylist()
                val mostPlayed = repository.getMostPlayedSongs(20)
                
                _libraryState.update {
                    it.copy(
                        isLoading = false,
                        recentSongs = recentSongs,
                        favorites = favorites,
                        mostPlayed = mostPlayed,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun onFavoritePlaylistClicked(playlistId: String, icon: String, title: String, creator: String) {
        navigate(
            Route.PlaylistDetail(playlistId, icon, title, creator, "FAVORITE")
        )
    }
    
    // ... other event handlers
}

data class LibraryScreenState(
    val isLoading: Boolean = false,
    val recentSongs: List<Song> = emptyList(),
    val favorites: List<Playlist> = emptyList(),
    val mostPlayed: List<Song> = emptyList(),
    val error: String? = null,
)
```

**Refactor all screen ViewModels similarly:**
- `PlaylistViewModel`
- `PlaylistDetailViewModel`
- `SearchViewModel`
- `AddPlaylistViewModel`
- `SettingsViewModel`

**Duration:** 5-7 days

---

### Phase 4: Move UI to Shared Multiplatform Layer

**Objectives:**
- Move all Composables from `androidApp/ui` to `shared/src/commonMain/ui`
- Adapt Android-specific code to use multiplatform APIs
- Create platform-specific UI variations if needed

**File Structure:**
```
shared/src/commonMain/
└── kotlin/com/rld/justlisten/ui/
    ├── screens/
    │   ├── library/
    │   │   ├── LibraryScreen.kt       ← Moved from androidApp
    │   │   ├── LibraryScreenContent.kt
    │   │   └── components/
    │   ├── playlist/
    │   │   ├── PlaylistScreen.kt
    │   │   ├── PlaylistContent.kt
    │   │   └── components/
    │   ├── playlistdetail/
    │   ├── search/
    │   ├── addplaylist/
    │   ├── settings/
    │   └── donation/
    ├── components/
    │   ├── LoadingIndicator.kt
    │   ├── ErrorScreen.kt
    │   └── common/
    ├── theme/
    │   ├── Theme.kt               ← Can share theme across platforms
    │   ├── Color.kt
    │   └── Typography.kt
    └── navigation/
        └── AppNavigation.kt
```

**Key Changes:**
1. Replace `AndroidMusicPlayer` references with `MusicPlayer` (interface)
2. Replace Android-specific imports with multiplatform equivalents
3. Move theme to `commonMain` for sharing
4. Create platform-specific UI overrides only when necessary

**Duration:** 5-7 days

---

### Phase 5: Create App-Level Integration Points

**Objectives:**
- Create main app composable
- Handle music player dependency injection
- Manage app-level state

**New File: `shared/src/commonMain/kotlin/com/rld/justlisten/ui/App.kt`**

```kotlin
@Composable
fun JustListenApp(
    musicPlayer: MusicPlayer,
    onNavigationEvent: (NavigationEvent) -> Unit = {},
) {
    val navController = rememberNavController()
    
    // Listen to navigation events from screens
    LaunchedEffect(navController) {
        // Handle navigation from ViewModels if needed
    }
    
    JustListenTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(
                bottomBar = { /* Bottom bar with navigation */ },
                content = { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    ) {
                        AppNavigation(
                            navController = navController,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                },
            )
        }
    }
}
```

**Duration:** 2-3 days

---

### Phase 6: Update Android App Entry Point

**Objectives:**
- Update Android app to use new architecture
- Set up dependency injection with Hilt (or Koin for multiplatform)
- Remove old navigation system

**Android Changes: `androidApp/src/main/java/com/rld/justlisten/android/MainActivity.kt`**

```kotlin
@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: Repository,
    private val musicPlayer: MusicPlayer,
) : ViewModel() {
    // Optional: app-level state if needed
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val musicPlayer = remember { AndroidMusicPlayer(...) }
            
            JustListenTheme {
                JustListenApp(
                    musicPlayer = musicPlayer,
                )
            }
        }
    }
}
```

**Duration:** 2 days

---

### Phase 7: Implement iOS with Compose Multiplatform

**Objectives:**
- Create iOS app using Compose Multiplatform
- Share same Composables with Android
- Set up iOS-specific DI (Koin)

**New File: `iosApp/iosApp/iOSApp.swift`**

```swift
import SwiftUI
import shared

@main
struct iOSApp: App {
    let koinApplication = KoinIOSKt.doInitKoin()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

**iOS will use the same Compose UI through the Compose Multiplatform framework.**

**Duration:** 7-10 days (complex due to iOS-Kotlin interop)

---

### Phase 8: Implement Dependency Injection (Koin or Hilt)

**Objectives:**
- Set up centralized DI
- Make it work across Android and iOS
- Remove manual factory functions

**Recommendation:** Use **Koin** for multiplatform support

**New File: `shared/src/commonMain/kotlin/com/rld/justlisten/di/AppModule.kt`**

```kotlin
fun createAppModule() = module {
    // Single instances
    single { Repository(get()) }
    single { MusicPlayer() }
    
    // ViewModels (can use viewModel scope)
    viewModelOf(::LibraryViewModel)
    viewModelOf(::PlaylistViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::AddPlaylistViewModel)
    viewModelOf(::PlaylistDetailViewModel)
    viewModelOf(::SettingsViewModel)
}

// Platform-specific modules would go in androidMain and iosMain
```

**Duration:** 2-3 days

---

### Phase 9: Data Layer Modernization (Optional but Recommended)

**Objectives:**
- Consider adding a repository pattern if not already clean
- Implement proper error handling
- Add caching layer if needed

**No changes needed if data layer is already clean, but consider:**
- Using `Result<T>` type instead of exceptions
- Implementing retry logic
- Adding offline support with caching

**Duration:** 3-5 days (optional)

---

### Phase 10: Testing & Quality Assurance

**Objectives:**
- Set up unit tests for ViewModels
- Add integration tests for navigation
- Add UI tests for critical screens

**Test Structure:**
```
shared/src/commonTest/
├── kotlin/com/rld/justlisten/
│   ├── viewmodel/
│   │   ├── LibraryViewModelTest.kt
│   │   ├── PlaylistViewModelTest.kt
│   │   └── ...
│   └── navigation/
│       └── AppNavigationTest.kt

androidApp/src/test/
└── ... Android-specific tests

androidApp/src/androidTest/
└── ... UI tests
```

**Duration:** 5-7 days

---

### Phase 11: Remove Legacy Code

**Objectives:**
- Remove old navigation system
- Delete `StateManager.kt`, `Navigation.kt`, `ScreenIdentifier.kt`, custom ViewModel
- Clean up Events class
- Remove unused dependencies

**Files to Delete:**
- `shared/src/commonMain/kotlin/com/rld/justlisten/StateManager.kt`
- `shared/src/commonMain/kotlin/com/rld/justlisten/Navigation.kt`
- `shared/src/commonMain/kotlin/com/rld/justlisten/ScreenIdentifier.kt`
- `shared/src/commonMain/kotlin/com/rld/justlisten/viewmodel/JustListenViewModel.kt`
- `shared/src/commonMain/kotlin/com/rld/justlisten/viewmodel/Events.kt`
- `shared/src/commonMain/kotlin/com/rld/justlisten/viewmodel/StateProvider.kt`
- `androidApp/src/main/java/com/rld/justlisten/android/ui/Router.kt`
- `androidApp/src/main/java/com/rld/justlisten/android/ui/screenpicker/ScreenPicker.kt`
- `androidApp/src/main/java/com/rld/justlisten/android/ui/OnePane.kt`
- All old screen files from `androidApp/ui` (moved to shared)

**Duration:** 2-3 days

---

### Phase 12: Documentation & Knowledge Transfer

**Objectives:**
- Document new architecture
- Create developer guide
- Record architecture decisions

**Deliverables:**
1. Architecture documentation with diagrams
2. Navigation guide
3. ViewModel creation template
4. Troubleshooting guide
5. Migration guide for any additional screens

**Duration:** 2-3 days

---

## Timeline Overview

| Phase | Duration | Complexity |
|-------|----------|-----------|
| Phase 0: Preparation | 1-2 days | Low |
| Phase 1: Dependencies | 1 day | Low |
| Phase 2: Navigation | 2 days | Medium |
| Phase 3: MVVM Refactor | 5-7 days | High |
| Phase 4: Move UI to Shared | 5-7 days | Medium |
| Phase 5: App Integration | 2-3 days | Low |
| Phase 6: Android Setup | 2 days | Low |
| Phase 7: iOS Implementation | 7-10 days | Very High |
| Phase 8: Dependency Injection | 2-3 days | Medium |
| Phase 9: Data Layer (Optional) | 3-5 days | Medium |
| Phase 10: Testing & QA | 5-7 days | Medium |
| Phase 11: Remove Legacy | 2-3 days | Low |
| Phase 12: Documentation | 2-3 days | Low |
| **TOTAL** | **40-60 days** | - |

**Note:** `*Timeline assumes 2-3 developers working in parallel on non-dependent tasks`

---

## Key Architecture Decisions

### 1. Navigation: Compose Navigation over Custom
- **Why:** Industry standard, well-tested, better tooling support
- **Benefits:** Deep linking support, SavedStateHandle, back stack management
- **Trade-off:** Learning curve, migration effort

### 2. State Management: StateFlow/SharedFlow over StateManager
- **Why:** Standard Kotlin/Jetpack approach
- **Benefits:** Testable, reactive, integrates with ViewModel
- **Trade-off:** Different pattern from current system

### 3. Dependency Injection: Koin for Multiplatform
- **Why:** Works on Android, iOS, and other platforms
- **Alternative:** Hilt (Android-only) + manual setup for iOS
- **Benefits:** Single DI system across platforms

### 4. ViewModel: Jetpack Compose ViewModel
- **Why:** Standard Android architecture
- **Benefits:** Lifecycle-aware, SavedStateHandle integration
- **Trade-off:** Requires jetpack-compose:lifecycle dependency

### 5. UI Sharing: Full Compose Multiplatform
- **Why:** Maximum code reuse, consistent UX
- **Benefits:** Single source of truth for UI
- **Trade-off:** Compose for iOS is still relatively new

---

## Risk Assessment & Mitigation

### High-Risk Areas

1. **iOS Compose Implementation**
   - *Risk:* iOS Compose is relatively new
   - *Mitigation:* Start with basic screens, use experimental features carefully
   - *Plan B:* Use SwiftUI for complex iOS-specific UI

2. **Navigation Complexity**
   - *Risk:* Current system is complex; migration could introduce bugs
   - *Mitigation:* Extensive testing, parallel feature branches
   - *Plan B:* Keep old navigation temporarily, gradual migration

3. **State Preservation**
   - *Risk:* Current system has complex state preservation
   - *Mitigation:* Leverage Compose Navigation's SavedStateHandle
   - *Testing:* Thorough testing of process death scenario

### Medium-Risk Areas

1. **Dependency Management**
   - *Mitigation:* Careful version management, gradual rollout
2. **Music Player Integration**
   - *Mitigation:* Abstract MusicPlayer interface, platform-specific impls
3. **Existing Users**
   - *Mitigation:* Beta release, gradual rollout with feature flags

---

## Success Criteria

- ✅ All tests passing (unit, integration, UI)
- ✅ Zero regression in functionality
- ✅ Performance equivalent or better
- ✅ iOS fully functional
- ✅ Code coverage > 80%
- ✅ Zero Android deprecation warnings
- ✅ Clean code with no legacy patterns
- ✅ Documentation complete

---

## After-Migration Maintenance

1. **Dependency Updates**
   - Compose: 1-2 major updates/year
   - Navigation: 1-2 major updates/year
   - ViewModel: Aligned with AndroidX releases

2. **Performance Monitoring**
   - Track recomposition metrics
   - Monitor memory usage
   - Watch for layout jank

3. **Future Improvements**
   - Add feature flags for gradual rollout
   - Implement analytics for user journey
   - Consider state persistence with DataStore

---

## Appendix: File-by-File Migration Map

### To Create (New)
- `navigation/Routes.kt` - Route definitions
- `navigation/AppNavigation.kt` - Navigation graph
- `ui/App.kt` - Main app composable
- `viewmodel/BaseScreenViewModel.kt` - Base ViewModel
- `viewmodel/library/LibraryViewModel.kt` - Screen VMs
- `di/AppModule.kt` - Koin setup
- `ui/screens/*/ScreenContent.kt` - Shared UI layering

### To Move (from androidApp/ui to shared/commonMain/ui)
- All screen directories
- All component files
- Theme files
- Extension utilities

### To Refactor (Existing)
- `shared/src/commonMain/viewmodel/screens/*` - Adapt to new pattern

### To Delete (Legacy)
- `StateManager.kt`
- `Navigation.kt`
- `ScreenIdentifier.kt`
- `JustListenViewModel.kt`
- `Events.kt`
- `StateProvider.kt`
- `androidApp/ui/Router.kt`
- `androidApp/ui/OnePane.kt`
- `androidApp/ui/screenpicker/*`

### To Keep (No Changes)
- Data layer (`datalayer/*`)
- Media layer (`media/*`)
- All database/repository code
- Resources and themes (move to shared)

---

## References & Resources

- [Compose Navigation Documentation](https://developer.android.com/guide/navigation/navigation-compose)
- [Jetpack ViewModel Best Practices](https://developer.android.com/topic/architecture)
- [Compose Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform.html)
- [Koin Documentation](https://insert-koin.io/)
- [StateFlow vs LiveData](https://developer.android.com/kotlin/flow)


