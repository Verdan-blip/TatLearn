package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Круговой индикатор: бледное кольцо и дуга 90°, которая вращается. */
@Composable
fun TamyrSpinner(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = TamyrSizes.spinner,
) {
    val rotation by rememberInfiniteTransition(label = "spinner").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(TamyrMotion.SPINNER_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "spinnerRotation",
    )
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = TamyrSizes.spinnerStroke.toPx()
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
        drawArc(
            color = color.copy(alpha = TRACK_ALPHA),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(strokeWidth),
        )
        drawArc(
            color = color,
            startAngle = rotation - 90f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(strokeWidth),
        )
    }
}

private const val TRACK_ALPHA = 0.35f

@Preview
@Composable
private fun TamyrSpinnerPreview() = PreviewFrame {
    TamyrSpinner(color = TamyrTheme.colors.primary)
}
