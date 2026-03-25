package dev.markturnip.radioplayer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PlaybackStateTest {

    @Test
    fun testAllStatesExist() {
        val states = PlaybackState.entries
        assertTrue(states.contains(PlaybackState.PLAYING))
        assertTrue(states.contains(PlaybackState.PAUSED))
        assertTrue(states.contains(PlaybackState.STOPPED))
        assertTrue(states.contains(PlaybackState.BUFFERING))
        assertTrue(states.contains(PlaybackState.ERROR))
    }

    @Test
    fun testStateEquality() {
        assertEquals(PlaybackState.PLAYING, PlaybackState.PLAYING)
        assertNotEquals(PlaybackState.PLAYING, PlaybackState.PAUSED)
    }

    @Test
    fun testStateNames() {
        assertEquals("PLAYING", PlaybackState.PLAYING.name)
        assertEquals("PAUSED", PlaybackState.PAUSED.name)
        assertEquals("STOPPED", PlaybackState.STOPPED.name)
        assertEquals("BUFFERING", PlaybackState.BUFFERING.name)
        assertEquals("ERROR", PlaybackState.ERROR.name)
    }

    @Test
    fun testStateCount() {
        assertEquals(5, PlaybackState.entries.size)
    }
}
