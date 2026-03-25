package dev.markturnip.radioplayer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MediaPlayerItemTest {

    private class TestMediaPlayerItem(
        override val id: String,
        override val title: String,
        override val artist: String?,
        override val url: String,
        override val isLive: Boolean,
        override val artworkUrl: String?
    ) : MediaPlayerItem

    @Test
    fun testLiveRadioStation() {
        val station = TestMediaPlayerItem(
            id = "station-1",
            title = "BBC Radio 1",
            artist = null,
            url = "https://stream.example.com/radio1.mp3",
            isLive = true,
            artworkUrl = "https://example.com/artwork.png"
        )
        assertEquals("station-1", station.id)
        assertEquals("BBC Radio 1", station.title)
        assertNull(station.artist)
        assertTrue(station.isLive)
        assertEquals("https://stream.example.com/radio1.mp3", station.url)
        assertEquals("https://example.com/artwork.png", station.artworkUrl)
    }

    @Test
    fun testOnDemandTrack() {
        val track = TestMediaPlayerItem(
            id = "track-42",
            title = "Blue (Da Ba Dee)",
            artist = "Eiffel 65",
            url = "https://stream.example.com/track42.mp3",
            isLive = false,
            artworkUrl = null
        )
        assertEquals("track-42", track.id)
        assertEquals("Eiffel 65", track.artist)
        assertFalse(track.isLive)
        assertNull(track.artworkUrl)
    }
}
