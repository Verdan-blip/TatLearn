package ru.tatarcha.tatlearn.feature.result

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion

/** Прогресс «набегания» XP от 0 до 1; при «уменьшить анимацию» сразу 1. */
@Composable
fun rememberXpCountProgress(reduceMotion: Boolean): State<Float> {
    val progress = remember { Animatable(if (reduceMotion) 1f else 0f) }
    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            progress.snapTo(1f)
        } else {
            delay(TamyrMotion.XP_COUNT_DELAY_MS.toLong())
            progress.animateTo(1f, tween(TamyrMotion.XP_COUNT_MS, easing = FastOutSlowInEasing))
        }
    }
    return progress.asState()
}
