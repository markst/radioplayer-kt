package dev.markturnip.radioplayer

import kotlin.test.Test
import kotlin.test.assertEquals

class PlatformMediaPlayerTest {

    private class TestMediaPlayerItem(
        override val id: String = "test-id",
        override val title: String = "Test Track",
        override val artist: String? = "Test Artist",
        override val url: String = "https://stream.example.com/test.mp3",
        override val isLive: Boolean = false,
        override val artworkUrl: String? = null
    ) : MediaPlayerItem

    @Test
    fun testPlayTransitionsToPlayingState() {
        val player = PlatformMediaPlayer()
        val states = mutableListOf<PlaybackState>()
        player.subscribeState { states += it }

        player.play()

        assertEquals(listOf(PlaybackState.PLAYING), states)
    }

    @Test
    fun testPauseTransitionsToPausedState() {
        val player = PlatformMediaPlayer()
        val states = mutableListOf<PlaybackState>()
        player.subscribeState { states += it }

        player.play()
        player.pause()

        assertEquals(listOf(PlaybackState.PLAYING, PlaybackState.PAUSED), states)
    }

    @Test
    fun testStopTransitionsToStoppedState() {
        val player = PlatformMediaPlayer()
        val states = mutableListOf<PlaybackState>()
        player.subscribeState { states += it }

        player.play()
        player.stop()

        assertEquals(listOf(PlaybackState.PLAYING, PlaybackState.STOPPED), states)
    }

    @Test
    fun testPlayItemTransitionsToPlayingState() {
        val player = PlatformMediaPlayer()
        val states = mutableListOf<PlaybackState>()
        player.subscribeState { states += it }

        player.playItem(TestMediaPlayerItem())

        assertEquals(listOf(PlaybackState.PLAYING), states)
    }
}
