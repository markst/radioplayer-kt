package dev.markturnip.radioplayer

data class Progress(val elapsed : Double, val duration : Double)

enum class PlaybackState {
    PLAYING, PAUSED, STOPPED, BUFFERING, ERROR
}

interface MediaPlayerItem {
    val title: String
    val artist: String?
    val url: String // Use String to be platform-agnostic
    val isLive: Boolean
    val artworkUrl: String?
}

interface PlatformMediaPlayerInterface {
    fun play()
}

expect final class PlatformMediaPlayer constructor() {
    fun playItem(mediaPlayerItem: MediaPlayerItem)
    fun play()
    fun pause()
    fun stop()

    fun skip(delta: Double) // skip
    fun seekWithPosition(position: Double)

    fun subscribeState(callback: (PlaybackState) -> Unit)
    fun subscribeProgress(callback: (Progress) -> Unit)
}
