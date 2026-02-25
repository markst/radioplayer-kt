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
    // Self-rescheduling progress poller: only re-posts itself while playing,
    // so it stops automatically when playback pauses or ends.
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
                    // STATE_READY fires on initial prepare and after seeks/buffering.
                    // Check isPlaying to set the correct initial state, but ongoing
                    // play/pause transitions while in STATE_READY are handled by
                    // onIsPlayingChanged below, since the state integer does not change.
                    Player.STATE_READY -> _state.value = if (exoPlayer.isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
                    Player.STATE_ENDED -> _state.value = PlaybackState.STOPPED
                }
            }

            // onPlaybackStateChanged does not re-fire when play()/pause() is called while
            // already in STATE_READY, so drive PLAYING/PAUSED state updates from here.
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    _state.value = PlaybackState.PLAYING
                    // Cancel any already-queued tick before posting a fresh one.
                    // onIsPlayingChanged(true) can fire multiple times per session
                    // (e.g. initial play, resume after seek, resume after buffering),
                    // so without removeCallbacks each call would spawn an extra loop.
                    handler.removeCallbacks(updateProgressRunnable)
                    handler.post(updateProgressRunnable)
                } else if (exoPlayer.playbackState == Player.STATE_READY) {
                    // Only emit PAUSED when the player is still ready (i.e. genuinely
                    // paused). Transitions to IDLE/ENDED are owned by onPlaybackStateChanged.
                    _state.value = PlaybackState.PAUSED
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

    // Use play()/pause() rather than playWhenReady so ExoPlayer applies the
    // command immediately and fires the correct listener callbacks.
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
