package ru.tatarcha.tatlearn.feature.module

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.icon.CheckIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.LessonRowState
import ru.tatarcha.tatlearn.feature.home.LockIcon

private const val VIEWPORT = 24f
private const val EGG = "M12 2.8 Q13.5 2.8 14.5 4.6 L21.7 16.9 Q22.8 19 20.4 19.9 Q12 21.7 3.6 19.9 Q1.2 19 2.3 16.9 L9.5 4.6 Q10.5 2.8 12 2.8 Z"
private const val HIGHLIGHT = "M8.6 7 Q6.4 10.6 4.6 14.4"
private const val SHADOW_SHIFT = 1.6f
private const val HALO_SCALE = 1.3f
private const val HALO_PIVOT_Y = 12.6f

/** Размер узла урока в ленте; «следующий» крупнее остальных. */
internal val LessonNodeSize = 48.dp
internal val NextLessonNodeSize = 58.dp
private val NumberLift = 3.dp

/** Узел-эчпочмак урока на ленте модуля: пройден (золотой с галочкой), следующий (крупный с номером), не начат и закрыт. */
@Composable
fun LessonNode(
    state: LessonRowState,
    number: Int,
    modifier: Modifier = Modifier,
) {
    val colors = TamyrTheme.colors
    val size = if (state == LessonRowState.Next) NextLessonNodeSize else LessonNodeSize
    val egg = remember { PathParser().parsePathString(EGG).toPath() }
    val highlight = remember { PathParser().parsePathString(HIGHLIGHT).toPath() }
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val factor = this.size.width / VIEWPORT
            withTransform({ scale(scaleX = factor, scaleY = factor, pivot = Offset.Zero) }) {
                when (state) {
                    LessonRowState.Done -> {
                        drawEgg(egg, fill = colors.goldText, stroke = colors.goldText, width = 1.5f, shifted = true)
                        drawEgg(egg, fill = colors.gold, stroke = colors.goldText, width = 1.5f)
                        drawPath(highlight, colors.surface.copy(alpha = 0.6f), style = Stroke(width = 1.2f, cap = StrokeCap.Round))
                    }
                    LessonRowState.Next -> {
                        withTransform({ scale(HALO_SCALE, HALO_SCALE, pivot = Offset(VIEWPORT / 2, HALO_PIVOT_Y)) }) {
                            drawEgg(egg, fill = colors.goldContainer, stroke = colors.gold, width = 1.3f)
                        }
                        drawEgg(egg, fill = colors.primaryShadow, stroke = colors.primaryShadow, width = 1.9f, shifted = true)
                        drawEgg(egg, fill = colors.surface, stroke = colors.primary, width = 1.9f)
                    }
                    LessonRowState.Available -> {
                        drawEgg(egg, fill = colors.borderStrong, stroke = colors.borderStrong, width = 1.5f, shifted = true)
                        drawEgg(egg, fill = colors.surface, stroke = colors.contour, width = 1.5f)
                    }
                    LessonRowState.Locked -> {
                        drawEgg(egg, fill = colors.borderStrong, stroke = colors.borderStrong, width = 1.5f, shifted = true)
                        drawEgg(egg, fill = colors.surfaceMuted, stroke = colors.contour, width = 1.5f)
                    }
                }
            }
        }
        when (state) {
            LessonRowState.Done -> CheckIcon(
                size = 20.dp,
                tint = colors.textPrimary,
                modifier = Modifier.offset(y = NumberLift),
            )
            LessonRowState.Next -> Text(
                text = number.toString(),
                style = TamyrTheme.typography.title22,
                color = colors.primary,
                modifier = Modifier.offset(y = NumberLift),
            )
            LessonRowState.Available -> Text(
                text = number.toString(),
                style = TamyrTheme.typography.body17Bold,
                color = colors.textSecondary,
                modifier = Modifier.offset(y = NumberLift),
            )
            LessonRowState.Locked -> Box(Modifier.offset(y = NumberLift)) { LockIcon(tint = colors.textSecondary) }
        }
    }
}

private fun DrawScope.drawEgg(
    egg: Path,
    fill: Color,
    stroke: Color,
    width: Float,
    shifted: Boolean = false,
) {
    withTransform({ if (shifted) translate(top = SHADOW_SHIFT) }) {
        drawPath(path = egg, color = fill)
        drawPath(path = egg, color = stroke, style = Stroke(width = width, join = StrokeJoin.Round))
    }
}

@Preview
@Composable
private fun LessonNodePreview() = PreviewFrame {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LessonNode(state = LessonRowState.Done, number = 1)
        LessonNode(state = LessonRowState.Next, number = 3)
        LessonNode(state = LessonRowState.Available, number = 4)
        LessonNode(state = LessonRowState.Locked, number = 5)
    }
}
