package ru.tatarcha.tatlearn.core.designsystem.component.icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Галочка из макета (SVG на сетке 24: 5,5·12,8 → 10·17,2 → 18,5·7,2, линия 2,8 со скруглением). */
@Composable
fun CheckIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = TamyrTheme.colors.onPrimary,
) {
    Canvas(modifier = modifier.size(size)) {
        val unit = this.size.width / GRID
        val path = Path().apply {
            moveTo(5.5f * unit, 12.8f * unit)
            lineTo(10f * unit, 17.2f * unit)
            lineTo(18.5f * unit, 7.2f * unit)
        }
        drawPath(path, tint, style = Stroke(width = STROKE * unit, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private const val GRID = 24f
private const val STROKE = 2.8f

@Preview
@Composable
private fun CheckIconPreview() = PreviewFrame { CheckIcon(tint = TamyrTheme.colors.primary) }
