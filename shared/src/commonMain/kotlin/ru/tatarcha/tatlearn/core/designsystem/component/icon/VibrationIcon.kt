package ru.tatarcha.tatlearn.core.designsystem.component.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val VibrationIconPaths = listOf(
    "M10 4 H14 A2 2 0 0 1 16 6 V18 A2 2 0 0 1 14 20 H10 A2 2 0 0 1 8 18 V6 A2 2 0 0 1 10 4 Z",
            "M4 8 V16 M20 8 V16",
)

@Composable
fun VibrationIcon(
    modifier: Modifier = Modifier,
    size: Dp = TamyrSizes.settingsIcon,
    tint: Color = TamyrTheme.colors.primary,
) {
    StrokeIcon(
        paths = VibrationIconPaths,
        size = size,
        tint = tint,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun VibrationIconPreview() = PreviewFrame {
    VibrationIcon()
}
