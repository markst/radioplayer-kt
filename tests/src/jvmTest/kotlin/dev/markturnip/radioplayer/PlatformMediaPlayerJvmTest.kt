package dev.markturnip.radioplayer

import kotlin.test.Test
import kotlin.test.assertEquals

class PlatformMediaPlayerJvmTest {

    @Test
    fun testInitialStateIsStopped() {
        val player = PlatformMediaPlayer()
        assertEquals(PlaybackState.STOPPED, player.currentState)
    }

    @Test
    fun testCurrentStateReflectsPlayPauseStop() {
        val player = PlatformMediaPlayer()
        player.play()
        assertEquals(PlaybackState.PLAYING, player.currentState)
        player.pause()
        assertEquals(PlaybackState.PAUSED, player.currentState)
        player.stop()
        assertEquals(PlaybackState.STOPPED, player.currentState)
    }

    @Test
    fun testSubscribeProgressReceivesEmittedUpdates() {
        val player = PlatformMediaPlayer()
        val received = mutableListOf<Progress>()
        player.subscribeProgress { received += it }

        player.emitProgress(Progress(elapsed = 10.0, duration = 180.0))
        player.emitProgress(Progress(elapsed = 20.0, duration = 180.0))

        assertEquals(2, received.size)
        assertEquals(10.0, received[0].elapsed)
        assertEquals(20.0, received[1].elapsed)
    }
}
