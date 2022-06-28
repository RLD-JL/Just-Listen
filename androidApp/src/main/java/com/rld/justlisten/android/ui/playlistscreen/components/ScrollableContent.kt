package com.rld.justlisten.android.ui.playlistscreen.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.rld.justlisten.android.ui.extensions.customTabIndicatorOffset
import com.rld.justlisten.android.ui.searchscreen.components.SearchGridTracks
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.viewmodel.screens.playlist.*

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
    )
    {
        Spacer(modifier = Modifier.height(50.dp))
        ListOfCollections(
            playlistState = playlistState, lasItemReached = lasItemReached,
            onPlaylistClicked = onPlaylistClicked
        )
        Spacer(modifier = Modifier.height(25.dp))


        val density = LocalDensity.current
        val list = getTrackCategory()
        val timeRangeList = getTimeRange()

        val tabWidths = remember {
            val tabWidthStateList = mutableStateListOf<Dp>()
            repeat(list.size) {
                tabWidthStateList.add(0.dp)
            }
            tabWidthStateList
        }

        val tabWidthsTimeRange = remember {
            val tabWidthStateList = mutableStateListOf<Dp>()
            repeat(timeRangeList.size) {
                tabWidthStateList.add(0.dp)
            }
            tabWidthStateList
        }


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
        Column(
            Modifier.padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTabTimeRange,
                backgroundColor = Color.Transparent,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.customTabIndicatorOffset(
                            currentTabPosition = tabPositions[selectedTabTimeRange],
                            tabWidth = tabWidthsTimeRange[selectedTabTimeRange]
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
                                tabWidthsTimeRange[selectedTabTimeRange] =
                                    with(density) { textLayoutResult.size.width.toDp() }
                            }
                        )
                    }
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