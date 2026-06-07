package com.rld.justlisten.ui.searchscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.rld.justlisten.ui.extensions.noRippleClickable
import com.rld.justlisten.ui.loadingscreen.LoadingScreen
import com.rld.justlisten.ui.components.MusicLoadingSpinner
import com.rld.justlisten.ui.components.MusicLoadingScreen
import com.rld.justlisten.ui.searchscreen.components.ShowPreviousSearches
import com.rld.justlisten.ui.searchscreen.components.ShowSearchResults
import com.rld.justlisten.ui.searchscreen.components.SearchCategoryDashboard
import com.rld.justlisten.ui.searchscreen.components.AutocompleteSuggestionsList
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.webservices.apis.searchcalls.AutocompleteUser
import com.rld.justlisten.viewmodel.screens.search.SearchScreenState
import com.rld.justlisten.viewmodel.screens.search.SearchSeeAllType
import com.rld.justlisten.viewmodel.screens.search.TrackItem
import com.rld.justlisten.ui.actions.SearchScreenAction

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalComposeUiApi
@Composable
fun SearchScreen(
    searchScreenState: SearchScreenState,
    onAction: (SearchScreenAction) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState(0)
    var active by rememberSaveable { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .noRippleClickable(onClick = { focusManager.clearFocus() })
    ) {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            if (searchScreenState.seeAllType == SearchSeeAllType.NONE) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchScreenState.searchFor,
                            onQueryChange = { onAction(SearchScreenAction.QueryChanged(it)) },
                            onSearch = {
                                active = false
                                onAction(SearchScreenAction.SearchPressed(it))
                            },
                            expanded = active,
                            onExpandedChange = { active = it },
                            placeholder = { Text("Search songs, artists, playlists...") },
                            leadingIcon = {
                                IconButton(onClick = { onAction(SearchScreenAction.BackPressed(false)) }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                            trailingIcon = {
                                if (searchScreenState.searchFor.isNotEmpty()) {
                                    IconButton(onClick = { onAction(SearchScreenAction.QueryChanged("")) }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                }
                            }
                        )
                    },
                    expanded = active,
                    onExpandedChange = { active = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (active) 0.dp else 16.dp, vertical = 8.dp),
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (searchScreenState.searchFor.isBlank()) {
                                ShowPreviousSearches(
                                    listOfSearches = searchScreenState.listOfSearches,
                                    onPreviousSearchedPressed = { searched ->
                                        active = false
                                        onAction(SearchScreenAction.SearchPressed(searched))
                                    }
                                )
                            } else {
                                if (searchScreenState.isAutocompleteLoading) {
                                    LoadingScreen(10.dp)
                                } else {
                                    AutocompleteSuggestionsList(
                                        tracks = searchScreenState.autocompleteTracks,
                                        playlists = searchScreenState.autocompletePlaylists,
                                        users = searchScreenState.autocompleteUsers,
                                        onSongPressed = { id, title, user, icon ->
                                            active = false
                                            onAction(SearchScreenAction.SongPressed(id, title, user, icon))
                                        },
                                        onPlaylistPressed = { id, icon, title, creator, fav ->
                                            active = false
                                            onAction(SearchScreenAction.PlaylistPressed(id, icon, title, creator, fav))
                                        },
                                        onUserPressed = { id, name ->
                                            active = false
                                            onAction(SearchScreenAction.ArtistClicked(id, name))
                                        }
                                    )
                                }
                            }
                        }
                    }
                )

                if (!active) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        when {
                            searchScreenState.isLoading -> {
                                LoadingScreen(10.dp)
                            }
                            searchScreenState.searchResultTracks.isNotEmpty() ||
                            searchScreenState.searchResultPlaylist.isNotEmpty() ||
                            searchScreenState.searchResultUsers.isNotEmpty() -> {
                                ShowSearchResults(
                                    searchQuery = searchScreenState.searchFor,
                                    searchResultUsers = searchScreenState.searchResultUsers,
                                    searchResultTracks = searchScreenState.searchResultTracks,
                                    searchResultPlaylist = searchScreenState.searchResultPlaylist,
                                    onSongPressed = { songId, title, user, songIcon ->
                                        onAction(SearchScreenAction.SongPressed(songId, title, user, songIcon))
                                    },
                                    onPlaylistPressed = { id, icon, title, createdBy, isFavorite ->
                                        onAction(SearchScreenAction.PlaylistPressed(id, icon, title, createdBy, isFavorite))
                                    },
                                    onUserPressed = { id, name ->
                                        onAction(SearchScreenAction.ArtistClicked(id, name))
                                    },
                                    onSeeAllClicked = { type ->
                                        onAction(SearchScreenAction.SeeAllClicked(type))
                                    }
                                )
                            }
                            else -> {
                                SearchCategoryDashboard(
                                    onCategoryClick = { category ->
                                        onAction(SearchScreenAction.SearchPressed(category))
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                SeeAllSearchContainer(
                    searchScreenState = searchScreenState,
                    onAction = onAction
                )
            }
        }
    }
}

@Composable
fun SeeAllSearchContainer(
    searchScreenState: SearchScreenState,
    onAction: (SearchScreenAction) -> Unit
) {
    val title = when (searchScreenState.seeAllType) {
        SearchSeeAllType.SONGS -> "Songs"
        SearchSeeAllType.PLAYLISTS -> "Playlists & Albums"
        SearchSeeAllType.ARTISTS -> "Artists"
        else -> ""
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onAction(SearchScreenAction.SeeAllClicked(SearchSeeAllType.NONE)) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        val isInitialLoading = searchScreenState.isSeeAllLoading && (
            (searchScreenState.seeAllType == SearchSeeAllType.SONGS && searchScreenState.seeAllTracks.isEmpty()) ||
            (searchScreenState.seeAllType == SearchSeeAllType.PLAYLISTS && searchScreenState.seeAllPlaylists.isEmpty()) ||
            (searchScreenState.seeAllType == SearchSeeAllType.ARTISTS && searchScreenState.seeAllUsers.isEmpty())
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isInitialLoading) {
                MusicLoadingScreen(showText = true)
            } else {
                when (searchScreenState.seeAllType) {
                SearchSeeAllType.SONGS -> {
                    val listState = rememberLazyListState()
                    val shouldLoadMore = remember {
                        derivedStateOf {
                            val totalItemsCount = listState.layoutInfo.totalItemsCount
                            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 5
                        }
                    }
                    LaunchedEffect(shouldLoadMore.value) {
                        if (shouldLoadMore.value && !searchScreenState.isSeeAllLoading && !searchScreenState.seeAllLastItemReached) {
                            onAction(SearchScreenAction.LoadMoreSeeAll)
                        }
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(searchScreenState.seeAllTracks) { track ->
                            TrackSeeAllRow(
                                track = track,
                                onClick = {
                                    onAction(
                                        SearchScreenAction.SongPressed(
                                            track.id,
                                            track.title,
                                            track.user,
                                            track.songIconList
                                        )
                                    )
                                },
                                onArtistClicked = { id, name ->
                                    onAction(SearchScreenAction.ArtistClicked(id, name))
                                }
                            )
                        }
                        if (searchScreenState.isSeeAllLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(66.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MusicLoadingScreen(
                                        size = 48.dp,
                                        showText = false
                                    )
                                }
                            }
                        }
                    }
                }
                SearchSeeAllType.PLAYLISTS -> {
                    val gridState = rememberLazyGridState()
                    val shouldLoadMore = remember {
                        derivedStateOf {
                            val totalItemsCount = gridState.layoutInfo.totalItemsCount
                            val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 6
                        }
                    }
                    LaunchedEffect(shouldLoadMore.value) {
                        if (shouldLoadMore.value && !searchScreenState.isSeeAllLoading && !searchScreenState.seeAllLastItemReached) {
                            onAction(SearchScreenAction.LoadMoreSeeAll)
                        }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(searchScreenState.seeAllPlaylists) { playlist ->
                            com.rld.justlisten.ui.playlistscreen.components.PlaylistRowItem(
                                playlistItem = playlist,
                                onPlaylistClicked = { id, icon, title, creator, fav ->
                                    onAction(SearchScreenAction.PlaylistPressed(id, icon, title, creator, fav))
                                },
                                onArtistClicked = { id, name ->
                                    onAction(SearchScreenAction.ArtistClicked(id, name))
                                }
                            )
                        }
                        if (searchScreenState.isSeeAllLoading) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MusicLoadingScreen(
                                            size = 120.dp,
                                            showText = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                SearchSeeAllType.ARTISTS -> {
                    val gridState = rememberLazyGridState()
                    val shouldLoadMore = remember {
                        derivedStateOf {
                            val totalItemsCount = gridState.layoutInfo.totalItemsCount
                            val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 9
                        }
                    }
                    LaunchedEffect(shouldLoadMore.value) {
                        if (shouldLoadMore.value && !searchScreenState.isSeeAllLoading && !searchScreenState.seeAllLastItemReached) {
                            onAction(SearchScreenAction.LoadMoreSeeAll)
                        }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(searchScreenState.seeAllUsers) { user ->
                            ArtistCircleSeeAllCard(
                                user = user,
                                onClick = {
                                    onAction(SearchScreenAction.ArtistClicked(user.id, user.name))
                                }
                            )
                        }
                        if (searchScreenState.isSeeAllLoading) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MusicLoadingScreen(
                                            size = 70.dp,
                                            showText = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
    }
}

@Composable
fun TrackSeeAllRow(
    track: TrackItem,
    onClick: () -> Unit,
    onArtistClicked: (String, String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageUrl = track.songIconList.songImageURL150px
        val painter = rememberAsyncImagePainter(imageUrl)
        
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground
            )
            val artistId = track._data.user.id
            Text(
                text = track.user,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (artistId.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.then(
                    if (artistId.isNotBlank()) {
                        Modifier.clickable { onArtistClicked(artistId, track.user) }
                    } else Modifier
                )
            )
        }

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ArtistCircleSeeAllCard(user: AutocompleteUser, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        val imageUrl = user.profilePicture?.songImageURL150px
        val painter = rememberAsyncImagePainter(imageUrl)
        
        if (!imageUrl.isNullOrBlank()) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier.size(70.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = user.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
