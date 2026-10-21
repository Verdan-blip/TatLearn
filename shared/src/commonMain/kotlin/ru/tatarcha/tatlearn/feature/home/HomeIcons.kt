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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// TODO(design): упрощённые приближения нарисованных от руки иконок макета; заменить экспортированными векторами.

private val IconSize = 20.dp
private val StrokeWidth = 2.dp

@Composable
fun HomeNavIcon(tint: Color) = Canvas(Modifier.size(IconSize)) {
    val w = size.width
    val roof = Path().apply {
        moveTo(w * 0.1f, w * 0.5f); lineTo(w * 0.5f, w * 0.1f); lineTo(w * 0.9f, w * 0.5f)
        lineTo(w * 0.8f, w * 0.5f); lineTo(w * 0.8f, w * 0.9f); lineTo(w * 0.2f, w * 0.9f); lineTo(w * 0.2f, w * 0.5f); close()
    }
    drawPath(roof, tint, style = Fill)
}

@Composable
fun BookNavIcon(tint: Color) = Canvas(Modifier.size(IconSize)) {
    val w = size.width
    drawRoundRect(
        tint, Offset(w * 0.15f, w * 0.05f), Size(w * 0.7f, w * 0.9f), CornerRadius(w * 0.15f),
        style = Stroke(StrokeWidth.toPx()),
    )
    drawLine(tint, Offset(w * 0.32f, w * 0.05f), Offset(w * 0.32f, w * 0.95f), StrokeWidth.toPx())
}

@Composable
fun TrophyNavIcon(tint: Color) = Canvas(Modifier.size(IconSize)) {
    val w = size.width
    val cup = Path().apply {
        moveTo(w * 0.2f, w * 0.1f); lineTo(w * 0.8f, w * 0.1f); lineTo(w * 0.7f, w * 0.55f)
        lineTo(w * 0.3f, w * 0.55f); close()
    }
    drawPath(cup, tint, style = Fill)
    drawRect(tint, Offset(w * 0.45f, w * 0.55f), Size(w * 0.1f, w * 0.2f))
    drawRect(tint, Offset(w * 0.25f, w * 0.78f), Size(w * 0.5f, w * 0.12f))
}

@Composable
fun PersonNavIcon(tint: Color) = Canvas(Modifier.size(IconSize)) {
    val w = size.width
    drawCircle(tint, radius = w * 0.2f, center = Offset(w * 0.5f, w * 0.28f))
    drawArc(tint, 180f, 180f, useCenter = true, topLeft = Offset(w * 0.1f, w * 0.55f), size = Size(w * 0.8f, w * 0.7f))
}

@Composable
fun BowlIcon(tint: Color) = Canvas(Modifier.size(width = 18.dp, height = 14.dp)) {
    drawArc(tint, 0f, 180f, useCenter = true, topLeft = Offset(0f, -size.height * 0.9f), size = Size(size.width, size.height * 1.8f))
}

@Composable
fun LockIcon(tint: Color) = Canvas(Modifier.size(width = 16.dp, height = 19.dp)) {
    val w = size.width
    val h = size.height
    drawArc(
        tint, 180f, 180f, useCenter = false, topLeft = Offset(w * 0.2f, 0f), size = Size(w * 0.6f, h * 0.6f),
        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
    )
    drawRoundRect(tint, Offset(0f, h * 0.4f), Size(w, h * 0.6f), CornerRadius(3.dp.toPx()))
}
