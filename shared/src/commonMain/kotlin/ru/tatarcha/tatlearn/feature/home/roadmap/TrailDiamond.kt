package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

private const val CORNER_RATIO = 0.25f
private const val OUTLINE_RATIO = 0.4f

/** Ромб тропы: пройденный золотой, будущий контурный. [half] это половина стороны неповёрнутого квадрата. */
internal fun DrawScope.drawTrailDiamond(
    center: Offset,
    half: Float,
    passed: Boolean,
    filledColor: Color,
    outlineColor: Color,
) {
    rotate(degrees = 45f, pivot = center) {
        val topLeft = Offset(center.x - half, center.y - half)
        val size = Size(half * 2, half * 2)
        val corner = CornerRadius(half * CORNER_RATIO)
        if (passed) {
            drawRoundRect(color = filledColor, topLeft = topLeft, size = size, cornerRadius = corner)
        } else {
            drawRoundRect(
                color = outlineColor,
                topLeft = topLeft,
                size = size,
                cornerRadius = corner,
                style = Stroke(width = half * OUTLINE_RATIO),
            )
        }
    }
}
