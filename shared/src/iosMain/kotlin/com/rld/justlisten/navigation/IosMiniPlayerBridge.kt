package com.rld.justlisten.navigation

import com.rld.justlisten.media.MediaMetadata
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.PlaybackState
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.media.RepeatMode
import com.rld.justlisten.datalayer.repositories.PlaylistRepository
import com.rld.justlisten.util.DeepLinkRouter
import com.rld.justlisten.util.parseJustListenDeepLink
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import platform.UIKit.UIColor
import platform.UIKit.UIViewController

data class IosMiniTrackState(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUrl: String,
)

data class IosMiniPlayerState(
    val visible: Boolean,
    val currentTrack: IosMiniTrackState?,
    val previousTrack: IosMiniTrackState?,
    val nextTrack: IosMiniTrackState?,
    val isPlaying: Boolean,
    val isBuffering: Boolean,
)

private fun MediaMetadata.toIosMiniTrackState() = IosMiniTrackState(
    id = id,
    title = title,
    artist = artist,
    artworkUrl = lowResArtworkUrl?.takeIf { it.isNotBlank() } ?: artworkUrl.orEmpty(),
)

private fun PlaybackState.toIosMiniPlayerState(
    playlist: List<MediaMetadata>,
): IosMiniPlayerState {
    val media = currentMedia
    val visible = status == PlaybackStatus.PLAYING ||
        status == PlaybackStatus.PAUSED ||
        status == PlaybackStatus.BUFFERING ||
        media != null
    val currentIndex = playlist.indexOfFirst { it.id == media?.id }
    val previous = when {
        currentIndex > 0 -> playlist.getOrNull(currentIndex - 1)
        currentIndex == 0 && repeatMode == RepeatMode.ALL -> playlist.lastOrNull()
        else -> null
    }
    val next = when {
        currentIndex in 0 until playlist.lastIndex -> playlist.getOrNull(currentIndex + 1)
        currentIndex == playlist.lastIndex && repeatMode == RepeatMode.ALL -> playlist.firstOrNull()
        else -> null
    }

    return IosMiniPlayerState(
        visible = visible,
        currentTrack = media?.toIosMiniTrackState(),
        previousTrack = previous?.toIosMiniTrackState(),
        nextTrack = next?.toIosMiniTrackState(),
        isPlaying = status == PlaybackStatus.PLAYING,
        isBuffering = status == PlaybackStatus.BUFFERING,
    )
}

private class IosPlayerStateObserverController(
    private val onStateChanged: (IosMiniPlayerState) -> Unit,
) : UIViewController(nibName = null, bundle = null) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.clearColor

        val musicPlayer = KoinPlatform.getKoin().get<MusicPlayer>()
        scope.launch {
            combine(musicPlayer.playbackState, musicPlayer.currentPlaylist) { playbackState, playlist ->
                playbackState.toIosMiniPlayerState(playlist)
            }
                .distinctUntilChanged()
                .collect { onStateChanged(it) }
        }
    }

}

private class IosSharedTrackDeepLinkObserverController(
    private val onTrackLoaded: () -> Unit,
    private val onNavigate: (Route) -> Unit,
) : UIViewController(nibName = null, bundle = null) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.clearColor

        val koin = KoinPlatform.getKoin()
        val musicPlayer = koin.get<MusicPlayer>()
        val playlistRepository = koin.get<PlaylistRepository>()

        scope.launch {
            DeepLinkRouter.deepLinkFlow.collect { url ->
                val deepLink = parseJustListenDeepLink(url) ?: return@collect
                when (deepLink.path) {
                    "track/share" -> {
                        val trackId = deepLink.parameters["id"] ?: return@collect
                        val track = runCatching {
                            playlistRepository.fetchTrackDetails(trackId)
                        }.getOrNull()

                        if (track != null) {
                            musicPlayer.loadMedia(
                                mediaId = track.id,
                                playlist = listOf(PlaylistItem(_data = track)),
                            )
                            onTrackLoaded()
                        } else {
                            com.rld.justlisten.ui.utils.showToast("Unable to open shared track")
                        }
                    }

                    "comments/share" -> {
                        val trackId = deepLink.parameters["trackId"] ?: return@collect
                        onNavigate(
                            Route.Comments(
                                trackId = trackId,
                                targetCommentId = deepLink.parameters["commentId"],
                            )
                        )
                    }
                }
            }
        }
    }

    fun dispose() = scope.cancel()
}

// A plain UIKit observer avoids creating another Compose/Metal surface beside
// the selected tab's ComposeUIViewController.
fun PlayerStateObserverViewController(
    onStateChanged: (IosMiniPlayerState) -> Unit,
): UIViewController = IosPlayerStateObserverController(onStateChanged)

fun SharedTrackDeepLinkObserverViewController(
    onTrackLoaded: () -> Unit,
    onNavigate: (Route) -> Unit,
): UIViewController = IosSharedTrackDeepLinkObserverController(onTrackLoaded, onNavigate)

fun disposeSharedTrackDeepLinkObserverViewController(
    controller: UIViewController,
) {
    (controller as? IosSharedTrackDeepLinkObserverController)?.dispose()
}

fun iosPlayerTogglePlayback() {
    val player = KoinPlatform.getKoin().get<MusicPlayer>()
    if (player.playbackState.value.status == PlaybackStatus.PLAYING) {
        player.pause()
    } else {
        player.play()
    }
}

fun iosPlayerSkipToNext() {
    KoinPlatform.getKoin().get<MusicPlayer>().skipToNext()
}

fun iosPlayerSkipToPrevious() {
    KoinPlatform.getKoin().get<MusicPlayer>().skipToPrevious()
}
