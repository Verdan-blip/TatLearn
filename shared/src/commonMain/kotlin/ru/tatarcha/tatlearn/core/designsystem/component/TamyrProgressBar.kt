package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

@Composable
fun TamyrProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = TamyrSizes.progressHeight,
    color: Color = TamyrTheme.colors.primary,
    trackColor: Color = TamyrTheme.colors.surfaceMuted,
    animationMillis: Int = TamyrMotion.PROGRESS_MS,
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(animationMillis))
    val shape = TamyrTheme.shapes.progress
    Box(modifier.fillMaxWidth().height(height).clip(shape).background(trackColor)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(animated).clip(shape).background(color))
    }
}
