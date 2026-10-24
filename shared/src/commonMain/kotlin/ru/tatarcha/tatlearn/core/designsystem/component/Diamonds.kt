package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Фирменный знак: квадрат, повёрнутый на 45°. Размер в разметке — неповёрнутый квадрат, как у CSS `rotate`. */
@Composable
fun Diamond(
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    scale: Float = 1f,
) {
    Box(
        modifier
            .size(size)
            .graphicsLayer {
                rotationZ = 45f
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
            .background(color),
    )
}

/** Терракота, золото, зелёный — фирменная тройка на онбординге и в шапках. */
@Composable
fun BrandDiamonds(modifier: Modifier = Modifier, size: Dp = TamyrSizes.diamondBrand) {
    val colors = TamyrTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(TamyrSizes.diamondGap), verticalAlignment = Alignment.CenterVertically) {
        Diamond(colors.accent, size)
        Diamond(colors.gold, size)
        Diamond(colors.primary, size)
    }
}

/**
 * Загрузчик: зелёный, золотой и терракотовый ромбы пульсируют по прозрачности .35→1 и масштабу .85→1.1,
 * период 1 с, каждый сдвинут на 0,2 с.
 */
@Composable
fun PulsingDiamonds(modifier: Modifier = Modifier) {
    val colors = TamyrTheme.colors
    val transition = rememberInfiniteTransition()
    val tints = listOf(colors.primary, colors.gold, colors.accent)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(TamyrSizes.loaderGap), verticalAlignment = Alignment.CenterVertically) {
        tints.forEachIndexed { index, tint ->
            // FastForward, чтобы сдвиг фазы был виден с первого кадра (CSS-задержка вместо этого простаивала бы).
            val phase = StartOffset(offsetMillis = index * PULSE_SHIFT_MS, offsetType = StartOffsetType.FastForward)
            val alpha = transition.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(PULSE_HALF_MS, easing = FastOutSlowInEasing), RepeatMode.Reverse, phase),
            )
            val scale = transition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(tween(PULSE_HALF_MS, easing = FastOutSlowInEasing), RepeatMode.Reverse, phase),
            )
            Diamond(tint, TamyrSizes.diamondLoader, alpha = alpha.value, scale = scale.value)
        }
    }
}

private const val PULSE_HALF_MS = 500
private const val PULSE_SHIFT_MS = 200
