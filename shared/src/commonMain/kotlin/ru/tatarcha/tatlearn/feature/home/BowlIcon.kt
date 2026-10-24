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

private val BowlWidth = 18.dp
private val BowlHeight = 14.dp

@Composable
fun BowlIcon(tint: Color) = Canvas(Modifier.size(width = BowlWidth, height = BowlHeight)) {
    drawArc(tint, 0f, 180f, useCenter = true, topLeft = Offset(0f, -size.height * 0.9f), size = Size(size.width, size.height * 1.8f))
}

@Preview
@Composable
private fun BowlIconPreview() = PreviewFrame { BowlIcon(TamyrTheme.colors.accent) }
