package com.example.audius.viewmodel.screens.addplaylist

import com.example.audius.datalayer.datacalls.addplaylistscreen.savePlaylist
import com.example.audius.viewmodel.Events

fun Events.addPlaylist(
    playlistName: String,
    playlistDescription: String?
) = dataRepository.savePlaylist(playlistName, playlistDescription)