package com.rld.justlisten.media.exoplayer

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import com.rld.justlisten.datalayer.utils.Constants.BASEURL
import com.rld.justlisten.datalayer.utils.Constants.appName
import kotlinx.coroutines.*
import java.io.IOException

@OptIn(UnstableApi::class)
class MusicPreloader(
    private val cacheDataSourceFactory: CacheDataSource.Factory,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private var preloadJob: Job? = null
    
    // Preload the first 512 KB of the next track (around 12.5 seconds of 320kbps MP3)
    private val PRELOAD_SIZE = 512 * 1024L 

    fun preloadSong(songId: String) {
        preloadJob?.cancel()
        
        preloadJob = scope.launch {
            // Debounce to prevent unnecessary downloads when the user is rapidly skipping tracks
            delay(2000L)
            
            val songUrl = "${BASEURL}/v1/tracks/${songId}/stream?app_name=$appName"
            val dataSpec = DataSpec.Builder()
                .setUri(Uri.parse(songUrl))
                .setKey(songId) // Crucial: must match the customCacheKey set in MediaMapper
                .setLength(PRELOAD_SIZE)
                .build()
                
            try {
                val cacheDataSource = cacheDataSourceFactory.createDataSource()
                val cacheWriter = CacheWriter(
                    cacheDataSource,
                    dataSpec,
                    null,
                    null
                )
                // Cache the block in the background thread
                cacheWriter.cache()
            } catch (e: CancellationException) {
                // Expected when coroutine is cancelled
            } catch (e: IOException) {
                // Ignore download or write errors during background preloading
            }
        }
    }

    fun cancel() {
        preloadJob?.cancel()
    }
}
