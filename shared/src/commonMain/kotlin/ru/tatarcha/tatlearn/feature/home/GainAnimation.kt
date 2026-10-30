package ru.tatarcha.tatlearn.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.ProgressGain
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.home_gain_chip

/**
 * «Доезд» прогресса после урока (макеты 39 и 40): пауза, полосы карточки и плитки растут, счётчик меняется,
 * золотое кольцо вспыхивает вокруг изменившихся карточки и плитки, над карточкой всплывает «+1 урок».
 * Пока [barsReached] ложно, экран показывает старые значения из [gain]; после анимации [gain] сбрасывается и показаны настоящие.
 */
@Stable
class GainAnimation(val gain: ProgressGain?, reduceMotion: Boolean = false) {
    var barsReached by mutableStateOf(gain == null)
    var counterReached by mutableStateOf(gain == null)
    val ring = Animatable(0f)
    val chip = Animatable(0f)

    /** При «уменьшить анимацию» полосы встают на итог сразу, без плавного роста. */
    val barMillis: Int = if (reduceMotion) 0 else TamyrMotion.GAIN_GROW_MS
}

@Composable
fun rememberGainAnimation(
    gain: ProgressGain?,
    reduceMotion: Boolean,
    onStarted: () -> Unit,
    onFinished: () -> Unit,
): GainAnimation {
    val animation = remember(gain, reduceMotion) { GainAnimation(gain, reduceMotion) }
    val started by rememberUpdatedState(onStarted)
    val finished by rememberUpdatedState(onFinished)
    LaunchedEffect(animation) {
        if (gain == null) return@LaunchedEffect
        started()
        if (reduceMotion) {
            animation.barsReached = true
            animation.counterReached = true
            animation.ring.snapTo(1f)
            animation.chip.snapTo(1f)
            delay(TamyrMotion.GAIN_REDUCED_HOLD_MS.toLong())
            animation.ring.snapTo(0f)
            animation.chip.snapTo(0f)
        } else {
            delay(TamyrMotion.GAIN_DELAY_MS.toLong())
            animation.barsReached = true
            delay(TamyrMotion.GAIN_GROW_MS.toLong())
            animation.counterReached = true
            coroutineScope {
                launch {
                    animation.chip.animateTo(1f, tween(TamyrMotion.GAIN_CHIP_IN_MS))
                    delay(TamyrMotion.GAIN_CHIP_HOLD_MS.toLong())
                    animation.chip.animateTo(0f, tween(TamyrMotion.GAIN_CHIP_OUT_MS))
                }
                animation.ring.animateTo(1f, tween(TamyrMotion.GAIN_RING_IN_MS))
                animation.ring.animateTo(0f, tween(TamyrMotion.GAIN_RING_OUT_MS))
            }
        }
        finished()
    }
    return animation
}

/** Золотое кольцо снаружи [shape] (без размытия, как «3D»-тень): рисуется поверх границы и не сдвигает содержимое. */
@Composable
fun Modifier.gainRing(animation: GainAnimation, shape: Shape): Modifier {
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
fun GainChip(animation: GainAnimation, modifier: Modifier = Modifier) {
    val lift = TamyrTheme.spacing.xs
    Text(
        text = stringResource(Res.string.home_gain_chip),
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
