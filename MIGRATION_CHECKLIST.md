# Compose Multiplatform Migration - Execution Checklist

## Quick Reference Checklist

### Pre-Migration Phase
- [ ] Create feature branch: `feature/compose-multiplatform-migration`
- [ ] Set up CI/CD to handle new dependencies
- [ ] Create parallel test environment
- [ ] Document current navigation flow
- [ ] Map all screen transitions
- [ ] Set up code review process for large PRs

### Phase 1: Dependencies
- [ ] Update `settings.gradle.kts` with new libraries
- [ ] Update `shared/build.gradle.kts`
- [ ] Update `androidApp/build.gradle.kts`
- [ ] Create `iosApp/build.gradle.kts` (if needed)
- [ ] Test: `./gradlew build` succeeds
- [ ] Verify no dependency conflicts

### Phase 2: Navigation Framework
- [ ] Create `navigation/Routes.kt` with all Route classes
- [ ] Create `navigation/AppNavigation.kt` with NavHost
- [ ] Add route serialization setup
- [ ] Test each route can be instantiated
- [ ] Create navigation test helpers

### Phase 3: ViewModel Refactoring (Per Screen)
For each screen (Library → Search → Settings order):
- [ ] Create `viewmodel/{screenname}/{ScreenName}ViewModel.kt`
- [ ] Create `viewmodel/{screenname}/{ScreenName}State.kt`
- [ ] Implement data loading logic
- [ ] Add event handlers (onClick, onSearch, etc)
- [ ] Write unit tests
- [ ] Verify state updates work
- [ ] Setup Hilt/Koin injection

### Phase 4: UI Migration (Per Screen)
For each screen:
- [ ] Create `ui/screens/{screenname}/` directory in shared
- [ ] Move Composable from androidApp to shared
- [ ] Remove Android-specific imports
- [ ] Replace with multiplatform equivalents
- [ ] Connect to new ViewModel
- [ ] Update event handlers to use ViewModel methods
- [ ] Test on Android
- [ ] Prepare iOS support

### Phase 5: Android Integration
- [ ] Update MainActivity
- [ ] Remove old App.kt lifecycle observer
- [ ] Setup Hilt for ViewModels
- [ ] Remove SaveableStateHolder usage
- [ ] Update dependency injection
- [ ] Remove Router.kt and OnePane.kt
- [ ] Test: App starts and navigates correctly

### Phase 6: iOS Implementation
- [ ] Create iOS app shell with Compose
- [ ] Setup iOS DI (Koin)
- [ ] Port music player for iOS
- [ ] Test: Each screen renders
- [ ] Test: Navigation works
- [ ] Test: State preservation works

### Phase 7: Advanced Features
- [ ] Implement deep linking
- [ ] Setup back button handling
- [ ] Add transition animations
- [ ] Implement SavedStateHandle for state persistence
- [ ] Add bottom sheet/modal handling

### Phase 8: Testing
- [ ] Unit tests for all ViewModels
- [ ] Integration tests for navigation
- [ ] UI tests for critical user journeys
- [ ] Test process death survival
- [ ] Test navigation backstack
- [ ] Performance testing

### Phase 9: Cleanup
- [ ] Delete StateManager.kt
- [ ] Delete Navigation.kt
- [ ] Delete ScreenIdentifier.kt
- [ ] Delete custom JustListenViewModel
- [ ] Delete Events.kt
- [ ] Delete unused screen init files
- [ ] Remove Hilt from old architecture
- [ ] Clean up imports

### Phase 10: Documentation & Release
- [ ] Document new architecture
- [ ] Create developer onboarding guide
- [ ] Record video tutorials
- [ ] Create troubleshooting guide
- [ ] Prepare release notes
- [ ] Beta test with select users
- [ ] Production release

---

## Per-Screen Migration Checklist

### Template: [ScreenName] Migration

```
Screen: ________
Current Location: androidApp/ui/________/
Status: [ ] Not Started [ ] In Progress [ ] Complete

ViewModel Creation:
  [ ] Create LibraryViewModel with all events from current screen
  [ ] Define LibraryScreenState data class
  [ ] Implement init block with data loading
  [ ] Write unit tests
  
Composable Migration:
  [ ] Move to shared/src/commonMain/ui/screens/library/
  [ ] Update to use ViewModel
  [ ] Remove Android-specific imports
  [ ] Connect state observation with collectAsState()
  [ ] Update event handlers
  [ ] Test on Android
  
Integration:
  [ ] Add to Routes.kt
  [ ] Add to AppNavigation.kt
  [ ] Setup DI injection
  [ ] Remove from old codebase
  [ ] Verify navigation to/from screen works
```

### Screen Migration Order (Recommended)

1. **Donation** (simplest, no interactive state)
2. **Settings** (simple state, good learning tool)
3. **Search** (input handling, search logic)
4. **Library** (list UI, complex interactions)
5. **Playlist** (list with filtering)
6. **PlaylistDetail** (complex nested state)
7. **AddPlaylist** (form handling, creation)

---

## Critical Code Reviews

Flag these for thorough review:

1. **Navigation setup** - Potential for bugs
2. **ViewModel lifecycle** - Memory leaks if not careful
3. **State preservation** - Must handle process death
4. **Event handling** - Threading/coroutine scope issues
5. **Dependencies** - Version conflicts
6. **Performance** - Recomposition spamming

---

## Rollback Plan

If critical issues arise:

1. Keep old code in `legacy/` branch
2. Tag current Android version before migration
3. Maintain parallel version for 2 weeks
4. Can revert to last stable tag if needed
5. For iOS: start fresh if issues (not critical yet)

---

## Definition of Done

Each screen's migration is complete when:

- ✅ ViewModel created and all events mapped
- ✅ All UI moved to shared
- ✅ Navigates correctly to/from screen
- ✅ State is preserved after navigation
- ✅ All previous functionality works
- ✅ Unit tests passing
- ✅ Zero crashes
- ✅ Performance not degraded
- ✅ Works on both Android and iOS
- ✅ Old code removed from codebase

---

## Communication Plan

- **Team Standup**: Daily updates during migration
- **Weekly Sync**: Architecture review, blockers discussion
- **Code Reviews**: Peer review all PRs, minimum 2 approvers
- **Demo**: Weekly demo of completed screens
- **Docs**: Updated architecture docs after each phase

---

## Common Pitfalls & Solutions

### Pitfall 1: Forgetting NavController in event handlers
**Problem:** Events don't navigate anywhere
```kotlin
// ❌ WRONG
fun onScreenClicked() {
    navigate(Route.PlaylistDetail(...))
}

// ✅ CORRECT
fun onScreenClicked(navController: NavController) {
    navController.navigate(Route.PlaylistDetail(...))
}
```

### Pitfall 2: State not updating in Compose
**Problem:** UI doesn't reflect state changes
```kotlin
// ❌ WRONG
private val _state = MutableStateFlow(State())
// Never update it!

// ✅ CORRECT
private val _state = MutableStateFlow(State())
val state = _state.asStateFlow()

fun updateState() {
    _state.update { it.copy(activeItem = newItem) }
}
```

### Pitfall 3: Memory leaks in ViewModels
**Problem:** Long-running operations in viewModelScope
```kotlin
// ❌ WRONG
fun loadData() {
    GlobalScope.launch { // WRONG! Never use GlobalScope
        val data = repository.fetch()
    }
}

// ✅ CORRECT
fun loadData() {
    viewModelScope.launch { // Automatically cancelled on VM destruction
        val data = repository.fetch()
    }
}
```

### Pitfall 4: Not handling back button
**Problem:** iOS and Android back behavior differs
```kotlin
// ✅ CORRECT for Android
BackHandler(enabled = canGoBack) {
    navController.popBackStack()
}

// ✅ CORRECT for iOS (handle in sheet dismiss)
```

### Pitfall 5: Not using SavedStateHandle
**Problem:** State lost on process death
```kotlin
// ❌ WRONG: No persistence
private val _state = MutableStateFlow(State())

// ✅ CORRECT: Persists state
class ViewModel(private val savedStateHandle: SavedStateHandle) {
    private val _state = MutableStateFlow(
        savedStateHandle.get<State>("state") ?: State()
    )
    
    fun updateState(newState: State) {
        _state.value = newState
        savedStateHandle["state"] = newState
    }
}
```

---

## Testing Strategy

### Unit Testing

```kotlin
@Test
fun testLibraryViewModelLoadsData() {
    val repository = MockRepository()
    val viewModel = LibraryViewModel(repository)
    
    val state = viewModel.libraryState.test()
    
    state.assertValues(
        LibraryScreenState(isLoading = true),
        LibraryScreenState(isLoading = false, items = expectedItems)
    )
}
```

### Navigation Testing

```kotlin
@Test
fun testNavigationToPlaylistDetail() {
    composeTestRule.setContent {
        val navController = rememberNavController()
        AppNavigation(navController = navController)
    }
    
    composeTestRule.onNodeWithText("Library").performClick()
    composeTestRule.onNodeWithText("Playlist Item").performClick()
    
    // Verify PlaylistDetail screen shown
    composeTestRule.onNodeWithText("Playlist Detail Header").assertIsDisplayed()
}
```

### Integration Testing

```kotlin
@Test
fun testCompleteUserJourney() {
    // Library -> Playlist -> PlaylistDetail -> back
    // Verify all transitions work
}
```

---

## Performance Checklist

After migration completion, verify:

- [ ] App startup time not increased
- [ ] Recomposition not excessive (use Recompose Inspector)
- [ ] Memory usage comparable to before
- [ ] No jank during scrolling
- [ ] Navigation transitions smooth
- [ ] Music playback not interrupted

---

## Post-Migration Monitoring

1. **Crash Analytics**: Track any new crashes
2. **Performance Metrics**: Compare before/after
3. **User Feedback**: Monitor ratings/reviews
4. **Error Logs**: Watch for unexpected errors
5. **Memory**: Check for memory leaks
6. **Battery**: Monitor battery impact

---

## Stakeholder Communication

### To Product Team
- Timeline: 40-60 days
- No new features during migration
- Beta release first
- More stable/performant than before

### To QA Team
- Different testing approach (ViewModel tests)
- Need comprehensive test coverage
- Process death testing critical
- Need to retest all screens

### To Users
- New architecture = faster, more stable
- More features in pipeline after this
- Beta period for feedback
- Gradual rollout to all users


