package com.rld.justlisten.viewmodel.interfaces

import com.rld.justlisten.datalayer.models.SongIconList

interface Item {
    val user: String
    val title: String
    val playlistTitle: String
    val id: String
    var isFavorite: Boolean
    var isReposted: Boolean
    val songIconList: SongIconList
    val songCounter: String
    val repostCount: Int
    val favoriteCount: Int
    val durationPlayedSec: Long
        get() = 0L
    val commentCount: Int
        get() = 0
    val playCount: Int
        get() = 0
    val duration: Int
        get() = 0
    val userId: String
        get() = ""
}