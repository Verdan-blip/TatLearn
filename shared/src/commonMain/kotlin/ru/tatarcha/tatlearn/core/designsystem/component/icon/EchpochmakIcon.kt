package ru.tatarcha.tatlearn.core.designsystem.component.icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private const val VIEWPORT = 24f
private const val BODY = "M12 2.6 Q14.3 2.6 15.7 5.2 L21.2 15.2 Q22.9 18.5 19.8 20.1 Q12 22.6 4.2 20.1 Q1.1 18.5 2.8 15.2 L8.3 5.2 Q9.7 2.6 12 2.6 Z"
private const val HIGHLIGHT = "M8.6 7 Q6.4 10.6 4.6 14.4"
private const val VEINS = "M12 12.6 Q12.4 9 12 5.4 M12 12.6 Q8.6 15 4.6 18.2 M12 12.6 Q15.4 15 19.4 18.2"

/** Эчпочмак (треугольный пирожок): пройденный урок золотой с «корочкой», непройденный пустой контур (макет 03). */
@Composable
fun EchpochmakIcon(
    filled: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = TamyrSizes.echpochmak,
) {
    val body = remember { PathParser().parsePathString(BODY).toPath() }
    val highlight = remember { PathParser().parsePathString(HIGHLIGHT).toPath() }
    val veins = remember { PathParser().parsePathString(VEINS).toPath() }
    val fill = if (filled) TamyrTheme.colors.gold else TamyrTheme.colors.surface.copy(alpha = EMPTY_FILL_ALPHA)
    val stroke = if (filled) TamyrTheme.colors.goldText else TamyrTheme.colors.contour
    val light = TamyrTheme.colors.surface.copy(alpha = HIGHLIGHT_ALPHA)
    val vein = TamyrTheme.colors.goldText.copy(alpha = VEIN_ALPHA)
    val core = TamyrTheme.colors.accentShadow
    Canvas(modifier = modifier.size(size)) {
        val factor = this.size.width / VIEWPORT
        withTransform({ scale(scaleX = factor, scaleY = factor, pivot = Offset.Zero) }) {
            drawPath(path = body, color = fill)
            drawPath(
                path = body,
                color = stroke,
                style = Stroke(width = if (filled) FILLED_STROKE else EMPTY_STROKE, join = StrokeJoin.Round),
            )
            if (filled) {
                drawPath(path = highlight, color = light, style = Stroke(width = FILLED_STROKE, cap = StrokeCap.Round))
                drawPath(path = veins, color = vein, style = Stroke(width = VEIN_STROKE, cap = StrokeCap.Round))
                drawCircle(color = core, radius = CORE_RADIUS, center = Offset(12f, 12.8f))
            }
        }
    }
}

private const val EMPTY_FILL_ALPHA = 0.7f
private const val HIGHLIGHT_ALPHA = 0.6f
private const val VEIN_ALPHA = 0.7f
private const val FILLED_STROKE = 1.5f
private const val EMPTY_STROKE = 1.6f
private const val VEIN_STROKE = 1.3f
private const val CORE_RADIUS = 1.7f

@Preview
@Composable
private fun EchpochmakIconPreview() = PreviewFrame {
    Row {
        EchpochmakIcon(filled = true)
        EchpochmakIcon(filled = false)
    }
}
