package ru.tatarcha.tatlearn.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val LockWidth = 16.dp
private val LockHeight = 19.dp
private val LockStroke = 2.dp
private val LockBodyRadius = 3.dp

@Composable
fun LockIcon(tint: Color) = Canvas(Modifier.size(width = LockWidth, height = LockHeight)) {
    val w = size.width
    val h = size.height
    drawArc(
        tint, 180f, 180f, useCenter = false, topLeft = Offset(w * 0.2f, 0f), size = Size(w * 0.6f, h * 0.6f),
        style = Stroke(LockStroke.toPx(), cap = StrokeCap.Round),
    )
    drawRoundRect(tint, Offset(0f, h * 0.4f), Size(w, h * 0.6f), CornerRadius(LockBodyRadius.toPx()))
}

@Preview
@Composable
private fun LockIconPreview() = PreviewFrame { LockIcon(TamyrTheme.colors.textSecondary) }
