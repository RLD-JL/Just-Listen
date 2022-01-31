package com.example.audius.viewmodel.interfaces

import com.example.audius.datalayer.models.SongIconList

interface Item {
    val user : String
    val title : String
    val playlistTitle : String
    val id : String
    var isFavorite: Boolean
    val songIconList : SongIconList
}