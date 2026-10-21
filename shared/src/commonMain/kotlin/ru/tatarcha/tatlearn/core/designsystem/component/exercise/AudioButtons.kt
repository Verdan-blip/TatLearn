package ru.tatarcha.tatlearn.core.designsystem.component.exercise

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val WaveHeights = listOf(14.dp, 24.dp, 18.dp, 28.dp, 12.dp)
private val WaveBarWidth = 4.dp
private val BigPlaySize = 72.dp
private val SlowIconSize = 14.dp

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

/** Большая круглая кнопка «звуковая волна» упражнения на аудирование. */
@Composable
fun WavePlayButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = TamyrTheme.colors
    Box(
        modifier
            .size(BigPlaySize)
            .solidShadow(colors.primaryShadow, 4.dp, TamyrTheme.shapes.circle)
            .clip(TamyrTheme.shapes.circle)
            .background(colors.primary)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            WaveHeights.forEach { h ->
                Box(Modifier.width(WaveBarWidth).height(h).clip(RoundedCornerShape(2.dp)).background(colors.onPrimary))
            }
        }
    }
}

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
