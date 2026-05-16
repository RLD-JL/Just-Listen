# Architecture Migration - Visual Summary

## Current Architecture (Before)

```
┌─────────────────────────────────────────────────────────────────┐
│                        Android App                               │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────┐
│   Presentation Layer (androidApp)    │
│  ┌─────────────────────────────────┐ │
│  │    MainActivity                 │ │
│  │    • Creates JustListenApp()    │ │
│  │    • Shows MainComposable       │ │
│  └─────────────────────────────────┘ │
│  ┌─────────────────────────────────┐ │
│  │    MainComposable               │ │
│  │    • Observes stateFlow         │ │
│  │    • Calls Router.OnePane()     │ │
│  └─────────────────────────────────┘ │
│  ┌─────────────────────────────────┐ │
│  │    Router.OnePane               │ │
│  │    • Bottom Bar + Scaffold      │ │
│  │    • Calls ScreenPicker         │ │
│  │    • Handles BackHandler        │ │
│  └─────────────────────────────────┘ │
│  ┌─────────────────────────────────┐ │
│  │    ScreenPicker (when switch)   │ │
│  │    • when screen.name            │ │
│  │    • Calls LibraryScreen()      │ │
│  │    • Passes events              │ │
│  └─────────────────────────────────┘ │
│  ┌─────────────────────────────────┐ │
│  │    Screen Composables (UI)      │ │
│  │    • LibraryScreen              │ │
│  │    • PlaylistScreen             │ │
│  │    • SettingsScreen             │ │
│  │    etc...                       │ │
│  └─────────────────────────────────┘ │
└──────────────────────────────────────┘

┌──────────────────────────────────────────────────┐
│   Shared Layer (shared module)                    │
│  ┌────────────────────────────────────────────┐  │
│  │   Custom State Management                  │  │
│  │  ┌─────────────────────────────────────┐  │  │
│  │  │  JustListenViewModel                │  │  │
│  │  │  • state: StateManager              │  │  │
│  │  │  • repository: Repository           │  │  │
│  │  │  • navigation: Navigation           │  │  │
│  │  │  • Factory.getAndroidInstance()     │  │  │
│  │  └─────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────┐  │  │
│  │  │  StateManager                       │  │  │
│  │  │  • screenStatesMap: Map<URI, State> │  │  │
│  │  │  • level1Backstack: List            │  │  │
│  │  │  • verticalBackstacks: Map          │  │  │
│  │  │  • screenScopesMap: Map<URI, Scope> │  │  │
│  │  │  • triggerRecomposition()           │  │  │
│  │  │  • addScreen/removeScreen           │  │  │
│  │  └─────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────┐  │  │
│  │  │  Navigation                         │  │  │
│  │  │  • navigate(Screen, params)         │  │  │
│  │  │  • exitScreen()                     │  │  │
│  │  │  • navigateByScreenIdentifier()     │  │  │
│  │  └─────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────┐  │  │
│  │  │  Events                             │  │  │
│  │  │  • saveSongToFavorites()            │  │  │
│  │  │  • screenCoroutine(block)           │  │  │
│  │  └─────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────┐  │  │
│  │  │  ScreenIdentifier                   │  │  │
│  │  │  • screen: Screen                   │  │  │
│  │  │  • URI: String (unique ID)          │  │  │
│  │  │  • params: ScreenParams             │  │  │
│  │  └─────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────┐  │
│  │   Screen Initialization (per-screen)       │  │
│  │  • libraryInitSettings                      │  │
│  │  • playlistInitSettings                     │  │
│  │  • ScreenInitSettings{}                     │  │
│  │    - title, initState, callOnInit           │  │
│  │    - reinitOnEachNavigation                 │  │
│  └────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────┐  │
│  │   Data Layer ✅ (Keep as-is)               │  │
│  │  • Repository                              │  │
│  │  • Database (SQLDelight)                   │  │
│  │  • Network (Ktor)                          │  │
│  └────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────┘

┌────────────────────────────────────┐
│        iOS App (Mostly Stub)       │
│  • Just displays "Hello"           │
│  • Not implemented                 │
└────────────────────────────────────┘
```

---

## Target Architecture (After)

```
┌─────────────────────────────────────────────────────────────────┐
│                    Compose Multiplatform                         │
│         (Same UI code runs on Android AND iOS)                   │
└─────────────────────────────────────────────────────────────────┘

                PLATFORM LAYER (Separate per platform)
┌──────────────────────┐                    ┌──────────────────────┐
│  Android App         │                    │  iOS App             │
│ ┌────────────────┐   │                    │ ┌────────────────┐   │
│ │  MainActivity  │   │                    │ │  iOSApp.swift  │   │
│ │  + Koin setup  │   │                    │ │  + Koin setup  │   │
│ └────────────────┘   │                    │ └────────────────┘   │
│ ┌────────────────┐   │                    │ ┌────────────────┐   │
│ │ Android DI     │   │                    │ │ iOS DI         │   │
│ │ (Hilt)         │   │                    │ │ (Koin)         │   │
│ └────────────────┘   │                    │ └────────────────┘   │
└──────────────────────┘                    └──────────────────────┘

                SHARED PRESENTATION LAYER (commonMain)
┌────────────────────────────────────────────────────────────────┐
│                    JustListenApp.kt                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ @Composable fun JustListenApp(                           │  │
│  │     navController: NavHostController,                    │  │
│  │     musicPlayer: MusicPlayer                            │  │
│  │ ) {                                                      │  │
│  │     Scaffold(                                            │  │
│  │         bottomBar = { BottomNavBar() },                 │  │
│  │         content = { AppNavigation(...) }                │  │
│  │     )                                                    │  │
│  │ }                                                        │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘

                STANDARD NAVIGATION (Compose Navigation)
┌────────────────────────────────────────────────────────────────┐
│                    AppNavigation.kt                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ @Composable fun AppNavigation(                           │  │
│  │     navController: NavHostController                     │  │
│  │ ) {                                                      │  │
│  │     NavHost(                                             │  │
│  │         composable<Route.Library> {                     │  │
│  │             LibraryScreen(...)                          │  │
│  │         }                                                │  │
│  │         composable<Route.Playlist> { ... }              │  │
│  │         composable<Route.PlaylistDetail> { ... }        │  │
│  │         // ... all routes                               │  │
│  │     )                                                    │  │
│  │ }                                                        │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                    │
│  Routes.kt                                                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ @Serializable sealed class Route {                       │  │
│  │     data object Library : Route()                        │  │
│  │     data object Playlist : Route()                       │  │
│  │     data class PlaylistDetail(...) : Route()             │  │
│  │     data object Search : Route()                         │  │
│  │     // ... etc                                           │  │
│  │ }                                                        │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘

                MODERN MVVM (Screen-Specific ViewModels)
┌──────────────────────────────────────────────────────────────────┐
│ Library Screen Flow                                               │
│ ┌────────────────────────────────┐                              │
│ │ LibraryScreen.kt               │                              │
│ │ • Composable UI component      │                              │
│ │ • Observes state flows         │                              │
│ │ • Calls viewModel methods      │                              │
│ └────────────────────────────────┘                              │
│              ↓                                                    │
│ ┌────────────────────────────────┐                              │
│ │ LibraryViewModel               │                              │
│ │ • Extends BaseScreenViewModel   │                              │
│ │ • loadLibraryData()            │                              │
│ │ • onFavoriteClicked()          │                              │
│ │ • Emits: navigationEvents      │                              │
│ │ • Exposes: libraryState        │                              │
│ └────────────────────────────────┘                              │
│              ↓                                                    │
│ ┌────────────────────────────────┐                              │
│ │ LibraryScreenState             │                              │
│ │ • isLoading: Boolean           │                              │
│ │ • recentSongs: List<Song>      │                              │
│ │ • favoritePlaylist: List       │                              │
│ │ • mostPlayedSongs: List        │                              │
│ │ • error: String?               │                              │
│ └────────────────────────────────┘                              │
│              ↓                                                    │
│ ┌────────────────────────────────┐                              │
│ │ Repository (Data Layer)        │                              │
│ │ • getRecentSongs()             │                              │
│ │ • getFavoritePlaylist()        │                              │
│ │ • getMostPlayedSongs()         │                              │
│ └────────────────────────────────┘                              │
└──────────────────────────────────────────────────────────────────┘

            SHARED UI LAYER (All composables in shared)
┌────────────────────────────────────────────────────────────────┐
│ ui/screens/                                                      │
│ ├── library/                                                     │
│ │   ├── LibraryScreen.kt (main composable)                      │
│ │   ├── LibraryScreenContent.kt (for testing)                   │
│ │   └── components/                                             │
│ │       ├── SongItem.kt                                         │
│ │       └── PlaylistItem.kt                                     │
│ ├── playlist/                                                    │
│ ├── search/                                                      │
│ ├── settings/                                                    │
│ ├── addplaylist/                                                │
│ ├── playlistdetail/                                             │
│ └── donation/                                                    │
│                                                                    │
│ components/                                                       │
│ ├── common/                                                      │
│ │   ├── LoadingIndicator.kt                                     │
│ │   └── ErrorScreen.kt                                          │
│                                                                    │
│ theme/                                                            │
│ ├── Theme.kt                                                     │
│ ├── Color.kt                                                     │
│ └── Typography.kt                                               │
└────────────────────────────────────────────────────────────────┘

        DEPENDENCY INJECTION (Koin for both platforms)
┌────────────────────────────────────────────────────────────────┐
│ di/AppModule.kt (Shared)                                         │
│ ┌──────────────────────────────────────────────────────────┐  │
│ │ fun appModule() = module {                              │  │
│ │     single { Repository() }                             │  │
│ │     single { LocationService() }                        │  │
│ │     viewModelOf(::LibraryViewModel)                     │  │
│ │     viewModelOf(::PlaylistViewModel)                    │  │
│ │     // ... all viewmodels                               │  │
│ │ }                                                        │  │
│ └──────────────────────────────────────────────────────────┘  │
│                                                                    │
│ androidMain/di/AndroidModule.kt                                  │
│ ┌──────────────────────────────────────────────────────────┐  │
│ │ fun androidModule() = module {                          │  │
│ │     single<MusicPlayer> {                               │  │
│ │         AndroidMusicPlayer(...)                         │  │
│ │     }                                                    │  │
│ │ }                                                        │  │
│ └──────────────────────────────────────────────────────────┘  │
│                                                                    │
│ iosMain/di/IosModule.kt                                          │
│ ┌──────────────────────────────────────────────────────────┐  │
│ │ fun iosModule() = module {                              │  │
│ │     single<MusicPlayer> {                               │  │
│ │         IosMusicPlayer(...)                             │  │
│ │     }                                                    │  │
│ │ }                                                        │  │
│ └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘

    DATA LAYER ✅ (Unchanged - Keep existing implementation)
┌────────────────────────────────────────────────────────────────┐
│ datalayer/                                                       │
│ ├── Repository.kt (Interface + Implementation)                  │
│ ├── models/ (Data classes)                                      │
│ ├── datacalls/ (HTTP/API calls)                                 │
│ ├── localdb/ (SQLDelight)                                       │
│ ├── webservices/ (Ktor client setup)                            │
│ └── utils/                                                       │
│                                                                    │
│ Database: SQLDelight (already working)                            │
│ Network: Ktor (already working)                                   │
└────────────────────────────────────────────────────────────────┘
```

---

## Data Flow Comparison

### Before: Complex Custom Flow

```
User clicks "Favorite Playlist"
           ↓
ScreenPicker { onFavoritePlaylistPressed() { navigate(...) } }
           ↓
Navigation.navigate(PlaylistDetail, params)
           ↓
StateManager.addScreen(screenIdentifier, initSettings)
           ↓
screenInitSettings.callOnInit(stateManager) { load data }
           ↓
stateManager.updateScreen(State::class) { state.copy(...) }
           ↓
stateManager.triggerRecomposition()
           ↓
mutableStateFlow.value = AppState(recompositionIndex++)
           ↓
MainComposable observes →  get Navigation → Router → OnePane → ScreenPicker
           ↓
ScreenPicker when switches to PlaylistDetail

❌ Complex, hard to track, many indirections
```

### After: Clear Modern Flow

```
User clicks "Favorite Playlist"
           ↓
LibraryScreen { onFavoriteClicked() }
           ↓
viewModel.onFavoritePlaylistClicked()
           ↓
navigate(Route.PlaylistDetail(...))
           ↓
navController.navigate(route)
           ↓
NavHost switches composable
           ↓
PlaylistDetailScreen appears
           ↓
PlaylistDetailViewModel init block runs
           ↓
state.update { copy(data = loadedData) }
           ↓
PlaylistDetailScreen observes state via collectAsState()
           ↓
UI updates with new data

✅ Linear flow, easy to follow, standard pattern
```

---

## Key Improvements Summary

| Aspect | Before | After |
|--------|--------|-------|
| Navigation | Custom `Navigation` class | Standard `Compose Navigation` |
| VM Pattern | Custom `JustListenViewModel` | Standard `Jetpack ViewModel` |
| State Mgmt | Manual `StateManager` | `StateFlow` + `MutableStateFlow` |
| Lifecycle | Manual management | Built-in `viewModelScope` |
| Testing | Hard to test | Easy to unit test |
| Platform | Android only | Android + iOS (Compose MP) |
| Code Sharing | Only data layer | UI + Logic + Data |
| Backstack | Manual lists & maps | Built-in navigation handling |
| Screen Init | Complex settings object | Simple init block in VM |
| DI | Hilt (Android) + manual | Koin (all platforms) |
| Recomposition | Manual triggers | Automatic via State |
| API | 5+ custom classes | 3 standard classes (ViewModel, State, Route) |

---

## Migration Impact Analysis

### ✅ What Stays the Same

- Data layer (Repository, Database)
- Network layer (Ktor client)
- Media player (mostly)
- UI components (can be reused)
- Material Design theme

### 🔄 What Changes

- Navigation system (biggest change)
- ViewModel approach (complete redesign)
- State management
- Composition structure
- Android entry point

### ➕ What Gets Added

- Compose Navigation
- Screen-specific ViewModels
- Jetpack Lifecycle integration
- iOS app
- Koin DI
- SavedStateHandle

### ❌ What Gets Removed

- StateManager
- Custom Navigation class
- Events class (replaced with ViewModel methods)
- ScreenIdentifier
- SaveableStateHolder management
- Manual backstack tracking
- Complex screen init settings

---

## Timeline Visualization

```
Week 1-2    Dependencies & Navigation Setup
├─ Phase 0-1: Adding dependencies, gradle setup
└─ Phase 2: Creating navigation framework
   
Week 3-6    ViewModel & UI Refactoring  
├─ Phase 3: Refactor to MVVM (most time)
└─ Phase 4: Move UI to shared
   
Week 7      Android Integration
├─ Phase 5-6: Update Android entry point
└─ Phase 8: Setup DI

Week 8      iOS Implementation
├─ Phase 7: Create iOS app shell
└─ Phase 7: iOS-specific setup

Week 9      Testing & Cleanup
├─ Phase 10: Testing (concurrent with UI)
└─ Phase 11-12: Cleanup & Documentation
```

---

## File Structure Changes

### Removed (Delete These)

```
❌ shared/src/commonMain/
   ├── StateManager.kt
   ├── Navigation.kt
   ├── ScreenIdentifier.kt
   └── viewmodel/
       ├── JustListenViewModel.kt
       ├── Events.kt
       └── StateProvider.kt

❌ androidApp/src/main/java/.../ui/
   ├── Router.kt
   ├── OnePane.kt
   ├── MainComposable.kt
   └── screenpicker/
```

### Created (Add These)

```
✨ shared/src/commonMain/
   ├── navigation/
   │   ├── Routes.kt
   │   └── AppNavigation.kt
   ├── ui/
   │   ├── App.kt
   │   ├── screens/
   │   │   ├── library/
   │   │   ├── playlist/
   │   │   ├── search/
   │   │   ├── settings/
   │   │   ├── addplaylist/
   │   │   ├── playlistdetail/
   │   │   └── donation/
   │   └── theme/
   ├── viewmodel/
   │   ├── BaseScreenViewModel.kt
   │   ├── NavigationEvent.kt
   │   ├── library/LibraryViewModel.kt
   │   ├── playlist/PlaylistViewModel.kt
   │   ├── search/SearchViewModel.kt
   │   ├── settings/SettingsViewModel.kt
   │   ├── addplaylist/AddPlaylistViewModel.kt
   │   └── playlistdetail/PlaylistDetailViewModel.kt
   └── di/
       └── AppModule.kt

✨ androidApp/
   └── MainActivity.kt (updated)

✨ iosApp/ (full implementation)
```

---

## Success Metrics

After migration, you should have:

1. ✅ **100% code coverage** for ViewModels
2. ✅ **0 crashes** on both platforms
3. ✅ **< 2 second** app startup
4. ✅ **Smooth animations** with no jank
5. ✅ **iOS fully functional** (not just a stub)
6. ✅ **Single UI codebase** for two platforms
7. ✅ **All existing features working**
8. ✅ **No deprecation warnings**
9. ✅ **Testable architecture**
10. ✅ **Easy onboarding** for new developers


