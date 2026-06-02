package com.rld.justlisten.datalayer.webservices.apis.searchcalls

import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse
import com.rld.justlisten.viewmodel.screens.search.SearchEnum
import com.rld.justlisten.viewmodel.screens.search.SearchEnum.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

suspend fun ApiClient.searchFor(searchFor: String, searchEnum: SearchEnum): PlayListResponse? {
    return when (searchEnum) {
        TRACKS -> {
            val response: PlayListResponse? =
                getResponse("/tracks/search?query=$searchFor")
            response?.let {
                PlayListResponse(it.data.filter { track -> track.isStreamable })
            }
        }
        PLAYLIST -> getResponse("/playlists/search?query=$searchFor")
    }
}

@Serializable
data class AutocompleteResponse(
    @SerialName("data") val data: AutocompleteData
)

@Serializable
data class AutocompleteData(
    @SerialName("tracks") val tracks: List<PlayListModel> = emptyList(),
    @SerialName("playlists") val playlists: List<PlayListModel> = emptyList(),
    @SerialName("albums") val albums: List<PlayListModel> = emptyList(),
    @SerialName("users") val users: List<AutocompleteUser> = emptyList()
)

@Serializable
data class AutocompleteUser(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("handle") val handle: String = "",
    @SerialName("profile_picture") val profilePicture: SongIconList? = null
)

suspend fun ApiClient.searchAutocomplete(query: String): AutocompleteResponse? {
    return getResponse("/search/autocomplete?query=$query")
}