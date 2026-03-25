package dev.markturnip.radioplayer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProgressTest {

    @Test
    fun testEquality() {
        val p1 = Progress(elapsed = 30.0, duration = 120.0)
        val p2 = Progress(elapsed = 30.0, duration = 120.0)
        assertEquals(p1, p2)
    }

    @Test
    fun testInequality() {
        val p1 = Progress(elapsed = 30.0, duration = 120.0)
        val p2 = Progress(elapsed = 60.0, duration = 120.0)
        assertNotEquals(p1, p2)
    }

    @Test
    fun testCopy() {
        val original = Progress(elapsed = 30.0, duration = 120.0)
        val updated = original.copy(elapsed = 60.0)
        assertEquals(60.0, updated.elapsed)
        assertEquals(120.0, updated.duration)
    }

    @Test
    fun testElapsedDurationRatio() {
        val progress = Progress(elapsed = 30.0, duration = 120.0)
        assertEquals(0.25, progress.elapsed / progress.duration)
    }

    @Test
    fun testZeroElapsedWithNonZeroDuration() {
        val progress = Progress(elapsed = 0.0, duration = 120.0)
        assertEquals(0.0, progress.elapsed)
        assertEquals(0.0, progress.elapsed / progress.duration)
    }
}
