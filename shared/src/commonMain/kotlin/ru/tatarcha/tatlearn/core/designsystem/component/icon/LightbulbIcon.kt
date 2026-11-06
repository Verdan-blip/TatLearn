package ru.tatarcha.tatlearn.core.designsystem.component.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val LightbulbPaths = listOf(
    "M9 18 H15 M10 21 H14",
    "M12 3 A6 6 0 0 0 8 13.5 C8.8 14.2 9 15 9 16 H15 C15 15 15.2 14.2 16 13.5 A6 6 0 0 0 12 3 Z",
)

/** Лампочка: метка карточки-правила («подсказка», а не «верно»). */
@Composable
fun LightbulbIcon(
    modifier: Modifier = Modifier,
    size: Dp = TamyrSizes.checkIcon,
    tint: Color = TamyrTheme.colors.goldText,
) {
    StrokeIcon(
        paths = LightbulbPaths,
        size = size,
        tint = tint,
        modifier = modifier,
        strokeWidth = 2.2f,
    )
}

@Preview
@Composable
private fun LightbulbIconPreview() = PreviewFrame {
    LightbulbIcon()
}
