package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.roadmap_gain_chip

/** Золотое кольцо снаружи [shape] (без размытия, как «3D»-тень): рисуется поверх границы и не сдвигает содержимое. */
@Composable
fun Modifier.gainRing(animation: RoadmapAnimation, shape: Shape): Modifier {
    val color = TamyrTheme.colors.gold
    val width = TamyrSizes.gainRing
    return drawBehind {
        val progress = animation.ring.value
        if (progress > 0f) {
            val stroke = width.toPx()
            val outline = shape.createOutline(Size(size.width + stroke, size.height + stroke), layoutDirection, this)
            translate(left = -stroke / 2, top = -stroke / 2) {
                drawOutline(outline = outline, color = color.copy(alpha = progress), style = Stroke(stroke))
            }
        }
    }
}

/** Подпись «+1 урок» над карточкой: всплывает снизу вверх и гаснет. */
@Composable
fun RoadmapGainChip(animation: RoadmapAnimation, modifier: Modifier = Modifier) {
    val lift = TamyrTheme.spacing.xs
    Text(
        text = stringResource(Res.string.roadmap_gain_chip),
        style = TamyrTheme.typography.label12Bold,
        color = TamyrTheme.colors.goldText,
        modifier = modifier
            .graphicsLayer {
                alpha = animation.chip.value
                translationY = (1f - animation.chip.value) * lift.toPx()
            }
            .clip(TamyrTheme.shapes.tag)
            .background(TamyrTheme.colors.goldContainer)
            .padding(horizontal = TamyrTheme.spacing.s, vertical = TamyrTheme.spacing.xxs),
    )
}
