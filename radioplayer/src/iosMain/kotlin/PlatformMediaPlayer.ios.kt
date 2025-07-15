package dev.markturnip.radioplayer
import dev.markturnip.radioplayer.MediaPlayController
import dev.markturnip.radioplayer.RadioPlayerStateBuffering
import dev.markturnip.radioplayer.RadioPlayerStatePaused
import dev.markturnip.radioplayer.RadioPlayerStatePlaying
import dev.markturnip.radioplayer.RadioPlayerStateStopped
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
actual class PlatformMediaPlayer actual constructor() : MediaPlayController() {
    actual fun playItem(mediaPlayerItem: MediaPlayerItem) {
        val url = NSURL(string = mediaPlayerItem.url)
        playWithUrl(url, at = null)
    }

    actual fun subscribeState(callback: (PlaybackState) -> Unit) {
        subscribeStateWithCallback { state ->
            when (state) {
                RadioPlayerStatePaused -> callback(PlaybackState.PAUSED)
                RadioPlayerStatePlaying -> callback(PlaybackState.PLAYING)
                RadioPlayerStateStopped -> callback(PlaybackState.STOPPED)
                RadioPlayerStateBuffering -> callback(PlaybackState.BUFFERING)
                else -> {
                    callback(PlaybackState.STOPPED)
                }
            }
        }
    }

    actual fun subscribeProgress(callback: (Progress) -> Unit) {
        subscribeProgressWithCallback { elapsed, duration ->
            callback(Progress(elapsed, duration))
        }
    }
}
