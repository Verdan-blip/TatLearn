package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Золотой скруглённый квадрат с длиной серии (главная 52dp, результат 48dp). */
@Composable
fun StreakBadge(
    days: Int,
    modifier: Modifier = Modifier,
    size: Dp = StreakBadgeSize,
    textStyle: TextStyle = TamyrTheme.typography.title20,
) {
    Box(
        modifier.size(size).clip(TamyrTheme.shapes.tile).background(TamyrTheme.colors.gold),
        contentAlignment = Alignment.Center,
    ) {
        Text(days.toString(), style = textStyle, color = TamyrTheme.colors.onAccent)
    }
}

private val StreakBadgeSize = 52.dp
