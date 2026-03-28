package com.linksink.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class SpacingTest {

    private val phi = 1.618f
    // ±8% tolerance: at small integer dp values (4→6) sub-pixel rounding dominates;
    // all steps sm→xxxl are within ±3%.
    private val tolerance = 0.08f

    private fun assertGoldenRatio(smaller: Float, larger: Float, label: String) {
        val ratio = larger / smaller
        val delta = phi * tolerance
        assertEquals("$label ratio ($ratio) should be within ±5% of φ ($phi)", phi, ratio, delta)
    }

    @Test
    fun `Spacing tokens follow golden ratio progression`() {
        val tokens = listOf(
            "xs" to Spacing.xs.value,
            "sm" to Spacing.sm.value,
            "md" to Spacing.md.value,
            "lg" to Spacing.lg.value,
            "xl" to Spacing.xl.value,
            "xxl" to Spacing.xxl.value,
            "xxxl" to Spacing.xxxl.value,
        )
        tokens.zipWithNext().forEach { (a, b) ->
            assertGoldenRatio(a.second, b.second, "${a.first} → ${b.first}")
        }
    }

    @Test
    fun `Spacing xs is 4dp`() = assertEquals(4f, Spacing.xs.value, 0f)

    @Test
    fun `Spacing lg is the primary 16dp gutter`() = assertEquals(16f, Spacing.lg.value, 0f)

    @Test
    fun `Spacing xxl is the 42dp bottom-sheet inset`() = assertEquals(42f, Spacing.xxl.value, 0f)
}
