package com.linksink.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Golden-ratio (φ ≈ 1.618) spacing scale.
 *
 * Each step is φ× the previous, rounded to the nearest even value that
 * preserves the visual progression on Android display densities:
 *
 *   xs  =  4 dp   (base)
 *   sm  =  6 dp   (4 × φ ≈ 6.5)
 *   md  = 10 dp   (6 × φ ≈ 9.7)
 *   lg  = 16 dp   (10 × φ ≈ 16.2)  ← primary screen-edge gutter
 *   xl  = 26 dp   (16 × φ ≈ 25.9)
 *   xxl = 42 dp   (26 × φ ≈ 42.1)  ← bottom-sheet safe-area inset
 *   xxxl= 68 dp   (42 × φ ≈ 67.9)
 */
object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 6.dp
    val md: Dp = 10.dp
    val lg: Dp = 16.dp
    val xl: Dp = 26.dp
    val xxl: Dp = 42.dp
    val xxxl: Dp = 68.dp
}

/**
 * Named constants for component-specific sizes that are not spacing tokens.
 * Extracted here to eliminate anonymous magic numbers without forcing them
 * into the layout spacing scale.
 */
object ComponentSize {
    val FaviconOuter: Dp = 40.dp
    val FaviconInner: Dp = 28.dp
    val FaviconCorner: Dp = 10.dp

    val AvatarSize: Dp = 36.dp
    val ChevronSize: Dp = 18.dp

    val TopicColorBarWidth: Dp = 20.dp
    val TopicColorBarHeight: Dp = 4.dp
    val TopicColorBarCorner: Dp = 2.dp

    val IllustrationCircle: Dp = 88.dp
    val IllustrationIcon: Dp = 44.dp

    val SyncIcon: Dp = 20.dp
    val SuccessIcon: Dp = 64.dp

    val ProgressIndicator: Dp = 20.dp
    val ProgressStrokeWidth: Dp = 2.dp

    val TopicListFixedHeight: Dp = 200.dp
}
