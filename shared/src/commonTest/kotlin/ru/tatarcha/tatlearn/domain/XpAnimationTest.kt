package ru.tatarcha.tatlearn.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class XpAnimationTest {
    @Test fun startAndEndOfTheSameLevel() {
        assertEquals(XpFrame(xp = 340, level = 4, barFraction = 0.4f), xpAnimationFrame(before = 340, after = 364, progress = 0f))
        assertEquals(XpFrame(xp = 364, level = 4, barFraction = 0.64f), xpAnimationFrame(before = 340, after = 364, progress = 1f))
    }

    @Test fun midFrameIsBetween() {
        assertEquals(352, xpAnimationFrame(before = 340, after = 364, progress = 0.5f).xp)
    }

    @Test fun crossingALevelWrapsTheBarQuietly() {
        val start = xpAnimationFrame(before = 392, after = 416, progress = 0f)
        assertEquals(4, start.level)
        assertEquals(0.92f, start.barFraction)
        val end = xpAnimationFrame(before = 392, after = 416, progress = 1f)
        assertEquals(5, end.level)
        assertEquals(0.16f, end.barFraction)
    }

    @Test fun progressIsClamped() {
        assertEquals(364, xpAnimationFrame(before = 340, after = 364, progress = 5f).xp)
        assertEquals(340, xpAnimationFrame(before = 340, after = 364, progress = -1f).xp)
    }
}
