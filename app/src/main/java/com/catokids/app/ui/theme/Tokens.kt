package com.catokids.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * One set of measurements for the whole app, so every screen shares the same rhythm.
 *
 * Spacing is a 4dp grid. Corners come in four sizes — chips, controls, cards, sheets —
 * and are always rounded: there are no sharp corners anywhere a child can touch.
 * Touch targets follow the guidance for young children, which is larger than adult
 * guidance: 56dp for any control and 72dp for game pieces a 3-year-old has to hit.
 */
object Space {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 28.dp
    val xxxl = 40.dp

    /** Side margin of every scrolling screen. */
    val gutter = 20.dp
}

object Radius {
    val chip = RoundedCornerShape(14.dp)
    val control = RoundedCornerShape(20.dp)
    val card = RoundedCornerShape(26.dp)
    val sheet = RoundedCornerShape(34.dp)
}

object Touch {
    val min = 56.dp
    val kid = 72.dp
}

/** Motion stays short and calm: things settle rather than bounce around. */
object MotionSpec {
    const val quick = 160
    const val normal = 260
    const val gentle = 420
}
