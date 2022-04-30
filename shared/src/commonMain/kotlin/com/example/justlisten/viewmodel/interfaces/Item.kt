package com.example.justlisten.viewmodel.interfaces

import com.example.justlisten.datalayer.models.SongIconList

interface Item {
    val user : String
    val title : String
    val playlistTitle : String
    val id : String
    var isFavorite: Boolean
    val songIconList : SongIconList
}