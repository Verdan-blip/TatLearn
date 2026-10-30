package ru.tatarcha.tatlearn.core.designsystem.component.exercise

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import ru.tatarcha.tatlearn.core.designsystem.component.solidShadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import kotlin.math.PI
import kotlin.math.sin

private val WaveHeights = listOf(14.dp, 24.dp, 18.dp, 28.dp, 12.dp)
private val WaveBarWidth = 4.dp
private val BigPlaySize = 72.dp
private val SlowIconSize = 14.dp
private const val WaveMinScale = 0.45f
private const val WavePhaseStep = 0.18f

/** Зелёная кнопка «треугольник в круге» рядом со словом или фразой. */
@Composable
fun PlayButton(onClick: () -> Unit, modifier: Modifier = Modifier, diameter: Dp = 46.dp) {
    val colors = TamyrTheme.colors
    Box(
        modifier.size(diameter).clip(TamyrTheme.shapes.circle).background(colors.primary).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(diameter * 0.4f)) {
            val path = Path().apply {
                moveTo(size.width * 0.15f, 0f)
                lineTo(size.width, size.height / 2)
                lineTo(size.width * 0.15f, size.height)
                close()
            }
            drawPath(path, colors.onPrimary)
        }
    }
}

/** Большая круглая кнопка «звуковая волна» упражнения на аудирование; пока [playing], столбики колеблются. */
@Composable
fun WavePlayButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    playing: Boolean = false,
) {
    val phase = rememberWavePhase(playing = playing)
    Box(
        modifier
            .size(BigPlaySize)
            .solidShadow(TamyrTheme.colors.primaryShadow, 4.dp, TamyrTheme.shapes.circle)
            .clip(TamyrTheme.shapes.circle)
            .background(TamyrTheme.colors.primary)
            .semantics { this.contentDescription = contentDescription }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            WaveHeights.forEachIndexed { index, height ->
                Box(
                    Modifier
                        .width(WaveBarWidth)
                        .height(if (playing) height * waveScale(phase + index * WavePhaseStep) else height)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TamyrTheme.colors.onPrimary),
                )
            }
        }
    }
}

@Composable
private fun rememberWavePhase(playing: Boolean): Float {
    if (!playing) return 0f
    val phase by rememberInfiniteTransition(label = "wave").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = TamyrMotion.WAVE_MS, easing = LinearEasing)),
        label = "wavePhase",
    )
    return phase
}

private fun waveScale(phase: Float): Float = WaveMinScale + (1f - WaveMinScale) * (0.5f + 0.5f * sin(2 * PI.toFloat() * phase))

/** «Помедленнее»: маленькая терракотовая иконка воспроизведения с подписью. */
@Composable
fun SlowPlayLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = TamyrTheme.colors.accent
    Row(
        modifier.clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(SlowIconSize)) {
            drawCircle(accent, radius = size.minDimension / 2 - 1.dp.toPx(), style = Stroke(2.dp.toPx()))
            val path = Path().apply {
                moveTo(size.width * 0.42f, size.height * 0.3f)
                lineTo(size.width * 0.72f, size.height * 0.5f)
                lineTo(size.width * 0.42f, size.height * 0.7f)
                close()
            }
            drawPath(path, accent)
        }
        Text(text, style = TamyrTheme.typography.body14Bold, color = accent)
    }
}
