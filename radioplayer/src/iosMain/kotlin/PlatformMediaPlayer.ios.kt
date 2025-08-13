package dev.markturnip.radioplayer

import dev.markst.radioplayer.MediaPlayController
import dev.markst.radioplayer.RadioPlayerStatePaused
import dev.markst.radioplayer.RadioPlayerStatePlaying
import dev.markst.radioplayer.RadioPlayerStateStopped
import dev.markst.radioplayer.RadioPlayerStateBuffering

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
