package com.rld.justlisten.viewmodel.interfaces

import com.rld.justlisten.datalayer.models.SongIconList

interface Item {
    val user : String
    val title : String
    val playlistTitle : String
    val id : String
    var isFavorite: Boolean
    val songIconList : SongIconList
}