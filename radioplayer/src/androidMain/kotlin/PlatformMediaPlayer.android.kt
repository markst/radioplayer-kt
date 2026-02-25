package dev.markturnip.radioplayer

import android.os.Handler
import android.os.Looper

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.filterNotNull

import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private const val POLL_INTERVAL_MS = 1000L // 1 second

actual final class PlatformMediaPlayer actual constructor() : KoinComponent {
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(get()).build()

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            val currentPosition = exoPlayer.currentPosition
            val duration = exoPlayer.duration
            _progress.value = Progress((currentPosition.toDouble() / 1000), (duration.toDouble() / 1000))
            if (exoPlayer.isPlaying) {
                handler.postDelayed(this, POLL_INTERVAL_MS)
            }
        }
    }

    private val _state = MutableStateFlow(PlaybackState.STOPPED)
    private val _progress = MutableStateFlow<Progress?>(null)

    // <editor-fold desc="Initialization">
    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_IDLE -> _state.value = PlaybackState.STOPPED
                    Player.STATE_BUFFERING -> _state.value = PlaybackState.BUFFERING
                    Player.STATE_READY -> _state.value = if (exoPlayer.isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
                    Player.STATE_ENDED -> _state.value = PlaybackState.STOPPED
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    _state.value = PlaybackState.PLAYING
                    handler.post(updateProgressRunnable)
                } else {
                    handler.removeCallbacks(updateProgressRunnable)
                    if (exoPlayer.playbackState == Player.STATE_READY) {
                        _state.value = PlaybackState.PAUSED
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.value = PlaybackState.ERROR
            }
        })
    }
    // </editor-fold>

    actual fun playItem(mediaPlayerItem: MediaPlayerItem) {
        val mediaItem = MediaItem.Builder()
            .setUri(mediaPlayerItem.url)
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        play()
    }

    actual fun play() {
        exoPlayer.play()
    }

    actual fun pause() {
        exoPlayer.pause()
    }

    actual fun stop() {
        exoPlayer.stop()
        _state.value = PlaybackState.STOPPED
        handler.removeCallbacks(updateProgressRunnable)
    }

    actual fun skip(delta: Double) {
        exoPlayer.seekTo(exoPlayer.currentPosition + delta.toLong() * 1000)
    }

    actual fun seekWithPosition(position: Double) {
        exoPlayer.seekTo((position * 1000).toLong())
    }

    actual fun subscribeState(callback: (PlaybackState) -> Unit) {
        coroutineScope.launch {
            _state.collect { playbackState ->
                callback(playbackState)
            }
        }
    }

    actual fun subscribeProgress(callback: (Progress) -> Unit) {
        coroutineScope.launch {
            _progress.filterNotNull().collect { progress ->
                callback(progress)
            }
        }
    }

    // Release ExoPlayer resources
    fun release() {
        exoPlayer.release()
        handler.removeCallbacksAndMessages(null)
        coroutineScope.cancel()
    }
}
