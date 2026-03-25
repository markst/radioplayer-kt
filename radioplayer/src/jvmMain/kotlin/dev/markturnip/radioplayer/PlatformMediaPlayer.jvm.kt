package dev.markturnip.radioplayer

actual final class PlatformMediaPlayer actual constructor() {

    private val stateCallbacks = mutableListOf<(PlaybackState) -> Unit>()
    private val progressCallbacks = mutableListOf<(Progress) -> Unit>()

    var currentState: PlaybackState = PlaybackState.STOPPED
        private set

    actual fun playItem(mediaPlayerItem: MediaPlayerItem) {
        updateState(PlaybackState.PLAYING)
    }

    actual fun play() {
        updateState(PlaybackState.PLAYING)
    }

    actual fun pause() {
        updateState(PlaybackState.PAUSED)
    }

    actual fun stop() {
        updateState(PlaybackState.STOPPED)
    }

    actual fun skip(delta: Double) {}

    actual fun seekWithPosition(position: Double) {}

    actual fun subscribeState(callback: (PlaybackState) -> Unit) {
        stateCallbacks += callback
    }

    actual fun subscribeProgress(callback: (Progress) -> Unit) {
        progressCallbacks += callback
    }

    fun emitProgress(progress: Progress) {
        progressCallbacks.forEach { it(progress) }
    }

    private fun updateState(newState: PlaybackState) {
        currentState = newState
        stateCallbacks.forEach { it(newState) }
    }
}
