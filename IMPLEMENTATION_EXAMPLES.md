# Code Examples: Implementing the New Architecture

This document provides concrete code examples to guide implementation of the migration.

## 1. Navigation Setup

### File: `shared/src/commonMain/kotlin/com/rld/justlisten/navigation/Routes.kt`

```kotlin
package com.rld.justlisten.navigation

import kotlinx.serialization.Serializable

/**
 * Sealed class hierarchy for all app routes.
 * Uses kotlinx.serialization for type-safe route passing.
 */
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

// Navigation levels for managing bottom bar vs modal screens
enum class NavigationLevel {
    LEVEL_1,   // Bottom bar tabs
    LEVEL_2,   // Modal/Detail screens
    LEVEL_3    // Nested modals
}

// Extension to determine navigation level for each route
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

// List of routes shown in bottom bar
val BOTTOM_BAR_ROUTES = listOf(
    Route.Library,
    Route.Playlist,
    Route.Search,
    Route.Settings,
    Route.Donation,
)

// Helper function to get title for bottom bar
fun Route.getBottomBarLabel(): String = when (this) {
    Route.Library -> "Library"
    Route.Playlist -> "Playlists"
    Route.Search -> "Search"
    Route.Settings -> "Settings"
    Route.Donation -> "Donate"
    else -> ""
}
```

### File: `shared/src/commonMain/kotlin/com/rld/justlisten/navigation/AppNavigation.kt`

```kotlin
package com.rld.justlisten.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.rld.justlisten.ui.screens.addplaylist.AddPlaylistScreen
import com.rld.justlisten.ui.screens.donation.DonationScreen
import com.rld.justlisten.ui.screens.library.LibraryScreen
import com.rld.justlisten.ui.screens.playlist.PlaylistScreen
import com.rld.justlisten.ui.screens.playlistdetail.PlaylistDetailScreen
import com.rld.justlisten.ui.screens.search.SearchScreen
import com.rld.justlisten.ui.screens.settings.SettingsScreen
import kotlin.reflect.typeOf

/**
 * Main navigation graph for the app.
 * Handles routing between all screens.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: Route = Route.Library,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { 1000 }) },
        exitTransition = { fadeOut() + slideOutHorizontally(targetOffsetX = { 1000 }) },
        popEnterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { -1000 }) },
        popExitTransition = { fadeOut() + slideOutHorizontally(targetOffsetX = { -1000 }) },
    ) {
        
        composable<Route.Library> {
            LibraryScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onBackPressed = {
                    navController.popBackStack()
                },
            )
        }
        
        composable<Route.Playlist> {
            PlaylistScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onBackPressed = {
                    navController.popBackStack()
                },
            )
        }
        
        composable<Route.PlaylistDetail> { backStackEntry ->
            val args: Route.PlaylistDetail = backStackEntry.toRoute()
            PlaylistDetailScreen(
                playlistId = args.playlistId,
                playlistIcon = args.playlistIcon,
                playlistTitle = args.playlistTitle,
                playlistCreatedBy = args.playlistCreatedBy,
                playlistEnum = args.playlistEnum,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onBackPressed = {
                    navController.popBackStack()
                },
            )
        }
        
        composable<Route.Search> {
            SearchScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onBackPressed = {
                    navController.popBackStack()
                },
            )
        }
        
        composable<Route.AddPlaylist> { backStackEntry ->
            val args: Route.AddPlaylist = backStackEntry.toRoute()
            AddPlaylistScreen(
                initialData = args.initialData,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onBackPressed = {
                    navController.popBackStack()
                },
            )
        }
        
        composable<Route.Settings> {
            SettingsScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onBackPressed = {
                    navController.popBackStack()
                },
            )
        }
        
        composable<Route.Donation> {
            DonationScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onBackPressed = {
                    navController.popBackStack()
                },
            )
        }
    }
}
```

---

## 2. ViewModel Base Class & Example

### File: `shared/src/commonMain/kotlin/com/rld/justlisten/viewmodel/BaseScreenViewModel.kt`

```kotlin
package com.rld.justlisten.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rld.justlisten.navigation.Route
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base class for all screen ViewModels.
 * Handles common functionality like navigation and loading states.
 */
abstract class BaseScreenViewModel : ViewModel() {
    
    protected val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvents: Flow<NavigationEvent> = _navigationEvents.receiveAsFlow()
    
    /**
     * Emit a navigation event to navigate to a route
     */
    protected fun navigate(route: Route) {
        viewModelScope.launch {
            _navigationEvents.send(NavigationEvent.NavigateTo(route))
        }
    }
    
    /**
     * Emit a back navigation event
     */
    protected fun popBackStack() {
        viewModelScope.launch {
            _navigationEvents.send(NavigationEvent.PopBackStack)
        }
    }
}

/**
 * Sealed interface for navigation events
 */
sealed interface NavigationEvent {
    data class NavigateTo(val route: Route) : NavigationEvent
    data object PopBackStack : NavigationEvent
}

/**
 * Sealed class for common UI states
 */
sealed class UiState {
    data object Loading : UiState()
    data class Success(val data: Any) : UiState()
    data class Error(val message: String) : UiState()
}

/**
 * Extension function for easier state updates
 */
inline fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}
```

### File: `shared/src/commonMain/kotlin/com/rld/justlisten/viewmodel/library/LibraryViewModel.kt`

```kotlin
package com.rld.justlisten.viewmodel.library

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.models.Playlist
import com.rld.justlisten.datalayer.models.Song
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Library screen.
 * Manages loading and displaying favorite playlists, recent songs, etc.
 */
class LibraryViewModel(
    private val repository: Repository,
) : BaseScreenViewModel() {
    
    private val _libraryState = MutableStateFlow(LibraryScreenState())
    val libraryState: StateFlow<LibraryScreenState> = _libraryState.asStateFlow()
    
    init {
        loadLibraryData()
    }
    
    /**
     * Load all library data on ViewModel initialization
     */
    private fun loadLibraryData() {
        viewModelScope.launch {
            try {
                _libraryState.update { it.copy(isLoading = true, error = null) }
                
                // Fetch data in parallel
                val recentSongsDeferred = viewModelScope.async {
                    repository.getRecentSongs(20)
                }
                val favoritesDeferred = viewModelScope.async {
                    repository.getFavoritePlaylist()
                }
                val mostPlayedDeferred = viewModelScope.async {
                    repository.getMostPlayedSongs(20)
                }
                
                val recentSongs = recentSongsDeferred.await()
                val favorites = favoritesDeferred.await()
                val mostPlayed = mostPlayedDeferred.await()
                
                _libraryState.update {
                    it.copy(
                        isLoading = false,
                        recentSongs = recentSongs,
                        favoritePlaylist = favorites,
                        mostPlayedSongs = mostPlayed,
                    )
                }
            } catch (e: Exception) {
                _libraryState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }
    
    /**
     * Handle favorite playlist item click
     */
    fun onFavoritePlaylistClicked(
        playlistId: String,
        playlistIcon: String,
        playlistTitle: String,
        playlistCreatedBy: String,
    ) {
        navigate(
            Route.PlaylistDetail(
                playlistId = playlistId,
                playlistIcon = playlistIcon,
                playlistTitle = playlistTitle,
                playlistCreatedBy = playlistCreatedBy,
                playlistEnum = "FAVORITE",
            )
        )
    }
    
    /**
     * Handle most played playlist click
     */
    fun onMostPlayedPlaylistClicked(
        playlistId: String,
        playlistIcon: String,
        playlistTitle: String,
        playlistCreatedBy: String,
    ) {
        navigate(
            Route.PlaylistDetail(
                playlistId = playlistId,
                playlistIcon = playlistIcon,
                playlistTitle = playlistTitle,
                playlistCreatedBy = playlistCreatedBy,
                playlistEnum = "MOST_PLAYED",
            )
        )
    }
    
    /**
     * Handle "View All Playlists" click
     */
    fun onAddPlaylistClicked() {
        navigate(Route.AddPlaylist())
    }
    
    /**
     * Load more recent songs when reaching end of list
     */
    fun loadMoreRecentSongs(currentCount: Int) {
        viewModelScope.launch {
            try {
                val moreSongs = repository.getRecentSongs(currentCount + 20)
                _libraryState.update { it.copy(recentSongs = moreSongs) }
            } catch (e: Exception) {
                _libraryState.update {
                    it.copy(error = "Failed to load more songs")
                }
            }
        }
    }
}

/**
 * UI State for Library screen
 */
data class LibraryScreenState(
    val isLoading: Boolean = false,
    val recentSongs: List<Song> = emptyList(),
    val favoritePlaylist: List<Playlist> = emptyList(),
    val mostPlayedSongs: List<Song> = emptyList(),
    val error: String? = null,
)
```

---

## 3. Shared Composable

### File: `shared/src/commonMain/kotlin/com/rld/justlisten/ui/screens/library/LibraryScreen.kt`

```kotlin
package com.rld.justlisten.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.library.LibraryScreenState
import com.rld.justlisten.viewmodel.library.LibraryViewModel

/**
 * Main Library Screen Composable
 * Displays recent songs, favorite playlists, and most played
 */
@Composable
fun LibraryScreen(
    onNavigate: (Route) -> Unit,
    onBackPressed: () -> Unit,
    viewModel: LibraryViewModel = viewModel(),
) {
    val state by viewModel.libraryState.collectAsState()
    
    // Observe navigation events
    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is com.rld.justlisten.viewmodel.NavigationEvent.NavigateTo -> {
                    onNavigate(event.route)
                }
                is com.rld.justlisten.viewmodel.NavigationEvent.PopBackStack -> {
                    onBackPressed()
                }
            }
        }
    }
    
    LibraryScreenContent(
        state = state,
        onFavoritePlaylistClicked = { id, icon, title, creator ->
            viewModel.onFavoritePlaylistClicked(id, icon, title, creator)
        },
        onMostPlayedClicked = { id, icon, title, creator ->
            viewModel.onMostPlayedPlaylistClicked(id, icon, title, creator)
        },
        onAddPlaylistClicked = {
            viewModel.onAddPlaylistClicked()
        },
        onLoadMoreRecentSongs = { currentCount ->
            viewModel.loadMoreRecentSongs(currentCount)
        },
    )
}

/**
 * Separate content composable for easier testing and preview
 */
@Composable
fun LibraryScreenContent(
    state: LibraryScreenState,
    onFavoritePlaylistClicked: (String, String, String, String) -> Unit,
    onMostPlayedClicked: (String, String, String, String) -> Unit,
    onAddPlaylistClicked: () -> Unit,
    onLoadMoreRecentSongs: (Int) -> Unit,
) {
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        
        state.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Error: ${state.error}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onLoadMoreRecentSongs(0) }) {
                        Text("Retry")
                    }
                }
            }
        }
        
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Recent Songs Section
                if (state.recentSongs.isNotEmpty()) {
                    item {
                        Text(
                            "Recent",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    items(state.recentSongs) { song ->
                        SongItem(song = song)
                    }
                }
                
                // Favorite Playlist Section
                if (state.favoritePlaylist.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "Favorites",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            IconButton(onClick = onAddPlaylistClicked) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Playlist",
                                )
                            }
                        }
                    }
                    items(state.favoritePlaylist) { playlist ->
                        PlaylistItem(
                            playlist = playlist,
                            onClick = {
                                onFavoritePlaylistClicked(
                                    playlist.id,
                                    playlist.icon,
                                    playlist.title,
                                    playlist.creator,
                                )
                            },
                        )
                    }
                }
                
                // Most Played Section
                if (state.mostPlayedSongs.isNotEmpty()) {
                    item {
                        Text(
                            "Most Played",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    items(state.mostPlayedSongs) { song ->
                        SongItem(song = song)
                    }
                }
            }
        }
    }
}

@Composable
fun SongItem(
    song: com.rld.justlisten.datalayer.models.Song,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, style = MaterialTheme.typography.bodyMedium)
                Text(song.artist, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun PlaylistItem(
    playlist: com.rld.justlisten.datalayer.models.Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(playlist.title, style = MaterialTheme.typography.bodyMedium)
                Text(playlist.creator, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
```

---

## 4. Dependency Injection Setup (Koin)

### File: `shared/src/commonMain/kotlin/com/rld/justlisten/di/AppModule.kt`

```kotlin
package com.rld.justlisten.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.viewmodel.library.LibraryViewModel
import com.rld.justlisten.viewmodel.playlist.PlaylistViewModel
import com.rld.justlisten.viewmodel.search.SearchViewModel
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import com.rld.justlisten.viewmodel.addplaylist.AddPlaylistViewModel
import com.rld.justlisten.viewmodel.playlistdetail.PlaylistDetailViewModel

/**
 * Koin module for app-wide dependency injection
 * Works on Android, iOS, and other platforms
 */
fun appModule() = module {
    // Single instances (app-wide singletons)
    single { Repository() }
    single { MusicPlayer() }
    
    // ViewModels (scoped to their lifecycle)
    viewModelOf(::LibraryViewModel)
    viewModelOf(::PlaylistViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AddPlaylistViewModel)
    viewModelOf(::PlaylistDetailViewModel)
}
```

### File: `shared/src/androidMain/kotlin/com/rld/justlisten/di/AndroidModule.kt`

```kotlin
package com.rld.justlisten.di

import android.content.Context
import com.rld.justlisten.media.AndroidMusicPlayer
import com.rld.justlisten.media.MusicPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific Koin module
 */
fun androidModule() = module {
    // Override MusicPlayer with Android implementation
    single<MusicPlayer> { AndroidMusicPlayer(androidContext()) }
}
```

### File: `androidApp/src/main/java/com/rld/justlisten/android/MainActivity.kt`

```kotlin
package com.rld.justlisten.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.rld.justlisten.di.appModule
import com.rld.justlisten.di.androidModule
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.navigation.AppNavigation
import com.rld.justlisten.ui.JustListenTheme
import org.koin.android.ext.android.inject
import org.koin.compose.KoinContext
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Koin if not already done
        if (GlobalContext.getKoin().getScopeOrNull("root") == null) {
            GlobalContext.startKoin {
                modules(appModule(), androidModule())
            }
        }
        
        installSplashScreen()
        
        setContent {
            JustListenTheme {
                JustListenAppNavigation()
            }
        }
    }
}

@Composable
fun JustListenAppNavigation() {
    val navController = rememberNavController()
    val musicPlayer: MusicPlayer by inject()
    
    KoinContext {
        AppNavigation(
            navController = navController,
        )
    }
}
```

---

## 5. Testing Examples

### File: `shared/src/commonTest/kotlin/com/rld/justlisten/viewmodel/LibraryViewModelTest.kt`

```kotlin
package com.rld.justlisten.viewmodel

import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.models.Playlist
import com.rld.justlisten.datalayer.models.Song
import com.rld.justlisten.viewmodel.library.LibraryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LibraryViewModel
    private lateinit var mockRepository: MockRepository
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockRepository()
        viewModel = LibraryViewModel(mockRepository)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun testInitialStateIsLoading() = runTest {
        // Given
        val testVM = LibraryViewModel(mockRepository)
        
        // When
        val state = testVM.libraryState.first()
        
        // Then
        assertTrue(state.isLoading)
    }
    
    @Test
    fun testLoadDataSuccess() = runTest {
        // When
        testScheduler.runCurrent()
        
        // Then
        val state = viewModel.libraryState.first()
        assertFalse(state.isLoading)
        assertEquals(3, state.recentSongs.size)
        assertEquals(2, state.favoritePlaylist.size)
    }
    
    @Test
    fun testOnFavoritePlaylistClickedNavigates() = runTest {
        // Given
        val events = mutableListOf<NavigationEvent>()
        
        // When
        viewModel.onFavoritePlaylistClicked("id", "icon", "title", "creator")
        
        // Then
        val event = viewModel.navigationEvents.first()
        assertIs<NavigationEvent.NavigateTo>(event)
    }
}

class MockRepository : Repository {
    override suspend fun getRecentSongs(limit: Int) = listOf(
        Song("1", "Song 1", "Artist 1"),
        Song("2", "Song 2", "Artist 2"),
        Song("3", "Song 3", "Artist 3"),
    )
    
    override suspend fun getFavoritePlaylist() = listOf(
        Playlist("fav1", "Favorites", "creator"),
        Playlist("fav2", "Liked", "creator"),
    )
    
    override suspend fun getMostPlayedSongs(limit: Int) = listOf(
        Song("101", "Hit 1", "Artist A"),
        Song("102", "Hit 2", "Artist B"),
    )
}
```

---

## Key Points Summary

1. **Routes are type-safe** - Compiler-checked navigation
2. **ViewModels handle logic** - Separation of concerns
3. **State flows are reactive** - UI updates automatically
4. **Navigation is explicit** - Easy to follow code flow
5. **Dependency injection is centralized** - Easy to manage
6. **Composables are testable** - Separate from logic
7. **Platform-specific code is isolated** - Reuse common code

---

## Migration Checklist for First Screen

When migrating a new screen (e.g., Settings):

1. [ ] Create `SettingsViewModel` following `LibraryViewModel` pattern
2. [ ] Create `SettingsScreenState` data class
3. [ ] Add `Route.Settings` to Routes.kt (if new)
4. [ ] Add screen to `AppNavigation.kt` NavHost
5. [ ] Move Composables from androidApp to `shared/ui/screens/settings/`
6. [ ] Update Composables to use new ViewModel
7. [ ] Remove Android-specific dependencies
8. [ ] Add unit tests for ViewModel
9. [ ] Test navigation to/from screen
10. [ ] Delete old files from androidApp

This pattern should be repeated for each screen!


