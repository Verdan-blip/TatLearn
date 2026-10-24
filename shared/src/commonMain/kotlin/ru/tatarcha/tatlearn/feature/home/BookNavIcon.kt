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

private const val STROKE_RATIO = 0.09f

/** Раскрытая книга: две страницы и корешок посередине. */
@Composable
fun BookNavIcon(tint: Color) = Canvas(Modifier.size(TamyrSizes.navIcon)) {
    val w = size.width
    val page = Path().apply {
        moveTo(w * 0.5f, w * 0.26f)
        cubicTo(w * 0.4f, w * 0.185f, w * 0.26f, w * 0.17f, w * 0.13f, w * 0.21f)
        lineTo(w * 0.13f, w * 0.76f)
        cubicTo(w * 0.26f, w * 0.72f, w * 0.4f, w * 0.735f, w * 0.5f, w * 0.81f)
        cubicTo(w * 0.6f, w * 0.735f, w * 0.74f, w * 0.72f, w * 0.87f, w * 0.76f)
        lineTo(w * 0.87f, w * 0.21f)
        cubicTo(w * 0.74f, w * 0.17f, w * 0.6f, w * 0.185f, w * 0.5f, w * 0.26f)
        close()
    }
    val stroke = Stroke(width = w * STROKE_RATIO, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawPath(page, tint, style = stroke)
    drawLine(tint, Offset(w * 0.5f, w * 0.26f), Offset(w * 0.5f, w * 0.81f), strokeWidth = w * STROKE_RATIO, cap = StrokeCap.Round)
}

@Preview
@Composable
private fun BookNavIconPreview() = PreviewFrame { BookNavIcon(TamyrTheme.colors.textSecondary) }
