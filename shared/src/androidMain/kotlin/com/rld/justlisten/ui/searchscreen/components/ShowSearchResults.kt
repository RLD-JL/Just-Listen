package com.rld.justlisten.ui.searchscreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.webservices.apis.searchcalls.AutocompleteUser
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.search.TrackItem
import com.rld.justlisten.viewmodel.screens.search.SearchSeeAllType

@Composable
fun ShowSearchResults(
    searchResultUsers: List<AutocompleteUser>,
    searchResultTracks: List<TrackItem>,
    searchResultPlaylist: List<PlaylistItem>,
    onSongPressed: (String, String, String, SongIconList) -> Unit,
    onPlaylistPressed: (String, String, String, String, Boolean) -> Unit,
    onUserPressed: (String) -> Unit,
    onSeeAllClicked: (SearchSeeAllType) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        if (searchResultUsers.isNotEmpty()) {
            SearchHeaderWithSeeAll(
                text = "Artists",
                onSeeAllClick = { onSeeAllClicked(SearchSeeAllType.ARTISTS) }
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(searchResultUsers.take(8)) { user ->
                    ArtistCard(user = user, onClick = { onUserPressed(user.name) })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (searchResultTracks.isNotEmpty()) {
            SearchHeaderWithSeeAll(
                text = "Songs",
                onSeeAllClick = { onSeeAllClicked(SearchSeeAllType.SONGS) }
            )
            SearchGridTracks(list = searchResultTracks.take(4), onSongPressed)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (searchResultPlaylist.isNotEmpty()) {
            SearchHeaderWithSeeAll(
                text = "Playlists & Albums",
                onSeeAllClick = { onSeeAllClicked(SearchSeeAllType.PLAYLISTS) }
            )
            PlaylistResult(playlist = searchResultPlaylist.take(6), onPlaylistPressed)
        }
    }
}

@Composable
fun SearchHeaderWithSeeAll(
    text: String,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "See All",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onSeeAllClick)
        )
    }
}

@Composable
fun ArtistCard(user: AutocompleteUser, onClick: () -> Unit) {
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
