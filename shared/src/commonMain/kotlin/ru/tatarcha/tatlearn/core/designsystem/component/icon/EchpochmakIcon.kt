package ru.tatarcha.tatlearn.core.designsystem.component.icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
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

/**
 * Эчпочмак (треугольный пирожок): пройденный урок золотой с «корочкой», непройденный пустой контур (макет 03).
 * [fill] от 0 до 1 заливает эчпочмак золотом снизу вверх: так он «румянится» после урока (анимация D).
 */
@Composable
fun EchpochmakIcon(
    filled: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = TamyrSizes.echpochmak,
    fill: Float = if (filled) 1f else 0f,
) {
    val body = remember { PathParser().parsePathString(BODY).toPath() }
    val highlight = remember { PathParser().parsePathString(HIGHLIGHT).toPath() }
    val veins = remember { PathParser().parsePathString(VEINS).toPath() }
    val colors = TamyrTheme.colors
    Canvas(modifier = modifier.size(size)) {
        val factor = this.size.width / VIEWPORT
        withTransform({ scale(scaleX = factor, scaleY = factor, pivot = Offset.Zero) }) {
            drawEmpty(body, colors.surface.copy(alpha = EMPTY_FILL_ALPHA), colors.contour)
            if (fill > 0f) {
                clipRect(top = VIEWPORT * (1f - fill), clipOp = ClipOp.Intersect) {
                    drawFilled(
                        body = body,
                        highlight = highlight,
                        veins = veins,
                        gold = colors.gold,
                        stroke = colors.goldText,
                        light = colors.surface.copy(alpha = HIGHLIGHT_ALPHA),
                        vein = colors.goldText.copy(alpha = VEIN_ALPHA),
                        core = colors.accentShadow,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawEmpty(body: Path, fill: Color, stroke: Color) {
    drawPath(path = body, color = fill)
    drawPath(path = body, color = stroke, style = Stroke(width = EMPTY_STROKE, join = StrokeJoin.Round))
}

private fun DrawScope.drawFilled(
    body: Path,
    highlight: Path,
    veins: Path,
    gold: Color,
    stroke: Color,
    light: Color,
    vein: Color,
    core: Color,
) {
    drawPath(path = body, color = gold)
    drawPath(path = body, color = stroke, style = Stroke(width = FILLED_STROKE, join = StrokeJoin.Round))
    drawPath(path = highlight, color = light, style = Stroke(width = FILLED_STROKE, cap = StrokeCap.Round))
    drawPath(path = veins, color = vein, style = Stroke(width = VEIN_STROKE, cap = StrokeCap.Round))
    drawCircle(color = core, radius = CORE_RADIUS, center = Offset(12f, 12.8f))
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
        EchpochmakIcon(filled = false, fill = 0.5f)
    }
}
