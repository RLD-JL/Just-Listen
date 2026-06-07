package com.rld.justlisten.media

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import android.net.Uri
import com.rld.justlisten.viewmodel.interfaces.Item
import com.rld.justlisten.datalayer.utils.Constants.BASEURL
import com.rld.justlisten.datalayer.utils.Constants.appName

fun Item.toMediaItem(): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(user)
        .setArtworkUri(Uri.parse(songIconList.songImageURL480px))
        .setDisplayTitle(title)
        .setSubtitle(user)
        .build()

    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(setSongUrl(id))
        .setCustomCacheKey(id)
        .setMediaMetadata(metadata)
        .build()
}

private fun setSongUrl(songId: String): String {
    return "${BASEURL}/v1/tracks/${songId}/stream?app_name=$appName"
}
