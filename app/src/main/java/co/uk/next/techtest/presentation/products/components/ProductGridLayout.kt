package co.uk.next.techtest.presentation.products.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor

fun gridColumnCount(
    screenWidthDp: Int,
    horizontalPadding: Dp,
    horizontalSpacing: Dp,
    minTileWidth: Dp,
    minColumns: Int,
    maxColumns: Int
): Int {
    val available = screenWidthDp.dp - (horizontalPadding * 2)
    val ratio: Float = (available + horizontalSpacing) / (minTileWidth + horizontalSpacing)
    val raw = floor(ratio.toDouble()).toInt()
    return raw.coerceIn(minColumns, maxColumns)
}
