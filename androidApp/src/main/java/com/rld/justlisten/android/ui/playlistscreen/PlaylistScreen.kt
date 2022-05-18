package com.rld.justlisten.android.ui.playlistscreen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.rld.justlisten.android.ui.loadingscreen.LoadingScreen
import com.rld.justlisten.android.ui.playlistscreen.components.PlaylistRowItem
import com.rld.justlisten.android.ui.theme.modifiers.horizontalGradientBackground
import com.rld.justlisten.android.ui.theme.typography
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.rld.justlisten.android.ui.extensions.customTabIndicatorOffset
import com.rld.justlisten.android.ui.searchscreen.SearchGridTracks
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.utils.Constants.list
import com.rld.justlisten.viewmodel.Events
import com.rld.justlisten.viewmodel.screens.playlist.*
import java.util.*

@Composable
fun PlaylistScreen(
    playlistState: PlaylistState,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit,
    onSearchClicked: () -> Unit,
    refreshScreen: () -> Unit,
    onSongPressed: (String, String, String, SongIconList) -> Unit,
    events: Events
) {
    if (playlistState.isLoading) {
        LoadingScreen()
    } else {
        val scrollState = rememberScrollState(0)
        val swipeRefreshState = rememberSwipeRefreshState(false)
        SwipeRefresh(state = swipeRefreshState, onRefresh = refreshScreen) {
            Box(modifier = Modifier.fillMaxSize()) {
                ScrollableContent(
                    lasItemReached = { lastIndex, playListEnum ->
                        when (playListEnum) {
                            PlayListEnum.TOP_PLAYLIST -> events.fetchPlaylist(
                                lastIndex,
                                PlayListEnum.TOP_PLAYLIST
                            )
                            PlayListEnum.REMIX -> events.fetchPlaylist(
                                lastIndex,
                                PlayListEnum.REMIX,
                                list[playlistState.queryIndex]
                            )
                            PlayListEnum.HOT -> events.fetchPlaylist(
                                lastIndex,
                                PlayListEnum.HOT,
                                list[playlistState.queryIndex2]
                            )
                            PlayListEnum.CURRENT_PLAYLIST -> TODO()
                            PlayListEnum.FAVORITE -> TODO()
                            PlayListEnum.CREATED_BY_USER -> TODO()
                        }
                    },
                    scrollState = scrollState,
                    playlistState = playlistState,
                    onPlaylistClicked = onPlaylistClicked,
                    onSongPressed = onSongPressed,
                    getNewTracks = { category, timeRange ->
                        events.getNewTracks(category, timeRange)
                    }
                )
                AnimatedToolBar(onSearchClicked)


            }
        }
    }
}

@Composable
fun AnimatedToolBar(
    onSearchClicked: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalGradientBackground(
                listOf(MaterialTheme.colors.background, MaterialTheme.colors.background)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {

        val rightNow = Calendar.getInstance()

        val text = when (rightNow.get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> "Chilling"
            in 5..11 -> "Good Morning"
            in 12..17 -> "Hey there"
            in 17..23 -> "Good Evening"
            else -> "Hello"
        }
        Header(text = text)
        Icon(
            modifier = Modifier.clickable(onClick = onSearchClicked),
            imageVector = Icons.Default.Search,
            contentDescription = null
        )
    }
}

@Composable
fun ScrollableContent(
    lasItemReached: (Int, PlayListEnum) -> Unit,
    scrollState: ScrollState,
    playlistState: PlaylistState,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit,
    onSongPressed: (String, String, String, SongIconList) -> Unit,
    getNewTracks: (TracksCategory, TimeRange) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        ListOfCollections(
            playlistState = playlistState, lasItemReached = lasItemReached,
            onPlaylistClicked = onPlaylistClicked
        )
        Spacer(modifier = Modifier.height(25.dp))


        val density = LocalDensity.current
        val list = getTrackCategory()

        val tabWidths = remember {
            val tabWidthStateList = mutableStateListOf<Dp>()
            repeat(list.size) {
                tabWidthStateList.add(0.dp)
            }
            tabWidthStateList
        }

        val timeRangeList = getTimeRange()

        var selectedTab by remember { mutableStateOf(0) }
        var selectedTabTimeRange by remember { mutableStateOf(0) }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = Color.Transparent,
            modifier = Modifier.padding(8.dp),
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.customTabIndicatorOffset(
                        currentTabPosition = tabPositions[selectedTab],
                        tabWidth = tabWidths[selectedTab]
                    )
                )
            }
        ) {
            list.fastForEachIndexed { index, item ->
                Tab(
                    modifier = Modifier.padding(bottom = 10.dp),
                    selected = index == selectedTab,
                    onClick = {
                        selectedTab = index
                        getNewTracks(item, timeRangeList[selectedTabTimeRange])
                    }
                )
                {
                    Text(
                        item.value,
                        onTextLayout = { textLayoutResult ->
                            tabWidths[selectedTab] =
                                with(density) { textLayoutResult.size.width.toDp() }
                        }
                    )
                }
            }
        }

        ScrollableTabRow(selectedTabIndex = selectedTabTimeRange,
            backgroundColor = Color.Transparent,
            modifier = Modifier.padding(8.dp),
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.customTabIndicatorOffset(
                        currentTabPosition = tabPositions[selectedTabTimeRange],
                        tabWidth = tabWidths[selectedTabTimeRange]
                    )
                )
            }) {
            timeRangeList.fastForEachIndexed { index, item ->
                Tab(
                    modifier = Modifier.padding(bottom = 10.dp),
                    selected = index == selectedTabTimeRange,
                    onClick = {
                        selectedTabTimeRange = index
                        getNewTracks(list[selectedTab], item)
                    }
                )
                {
                    Text(
                        item.value,
                        onTextLayout = { textLayoutResult ->
                            tabWidths[selectedTabTimeRange] =
                                with(density) { textLayoutResult.size.width.toDp() }
                        }
                    )
                }
            }
        }
        Column(
            modifier = if (playlistState.tracksLoading) Modifier
                .height(500.dp)
                .fillMaxWidth() else Modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (playlistState.tracksLoading) {
                CircularProgressIndicator()
            } else {
                SearchGridTracks(list = playlistState.tracksList, onSongPressed = onSongPressed)
            }
        }
    }
}

@Composable
fun Header(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = typography.h5.copy(fontWeight = FontWeight.ExtraBold),
        modifier = modifier.padding(start = 8.dp, end = 4.dp, bottom = 8.dp, top = 24.dp)
    )
}

@Composable
fun ListOfCollections(
    playlistState: PlaylistState, lasItemReached: (Int, PlayListEnum) -> Unit,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit
) {
    val list = remember {
        mutableListOf(
            "Top Playlist",
            list[playlistState.queryIndex],
            list[playlistState.queryIndex2]
        )
    }
    list.fastForEachIndexed { index, item ->
        Header(text = item)
        when (index) {
            0 -> PlaylistRow(
                playlist = playlistState.playlistItems,
                lasItemReached = lasItemReached,
                PlayListEnum.TOP_PLAYLIST,
                onPlaylistClicked,
                playlistState.lastFetchPlaylist
            )
            1 -> PlaylistRow(
                playlist = playlistState.remixPlaylist,
                lasItemReached = lasItemReached,
                PlayListEnum.REMIX,
                onPlaylistClicked,
                playlistState.lastFetchRemix
            )
            2 -> PlaylistRow(
                playlist = playlistState.hotPlaylist,
                lasItemReached = lasItemReached,
                PlayListEnum.HOT,
                onPlaylistClicked,
                playlistState.lastFetchHot
            )
        }
    }
}

@Composable
fun PlaylistRow(
    playlist: List<PlaylistItem>, lasItemReached: (Int, PlayListEnum) -> Unit,
    playlistEnum: PlayListEnum,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit,
    lastIndexReached: Boolean = false
) {
    val fetchMore = remember { mutableStateOf(false) }
    LazyRow(verticalAlignment = Alignment.CenterVertically) {
        itemsIndexed(items = playlist) { index, playlistItem ->

            if (index == playlist.lastIndex && !lastIndexReached) {
                LaunchedEffect(key1 = playlist.lastIndex)
                {
                    lasItemReached(index + 20, playlistEnum)
                    fetchMore.value = true
                }
            }

            if (lastIndexReached) {
                fetchMore.value = false
            }

            PlaylistRowItem(
                playlistItem = playlistItem,
                onPlaylistClicked = onPlaylistClicked
            )
        }
        if (fetchMore.value) {
            item {
                CircularProgressIndicator()
            }
        }
    }
}
