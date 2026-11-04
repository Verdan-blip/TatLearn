package ru.tatarcha.tatlearn.core.designsystem.component.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val BellIconPaths = listOf(
    "M6 17 V11 Q6 6 12 6 Q18 6 18 11 V17 L19.5 18.5 H4.5 Z",
            "M10 21 Q12 22.5 14 21",
)

@Composable
fun BellIcon(
    modifier: Modifier = Modifier,
    size: Dp = TamyrSizes.settingsIcon,
    tint: Color = TamyrTheme.colors.primary,
) {
    StrokeIcon(
        paths = BellIconPaths,
        size = size,
        tint = tint,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun BellIconPreview() = PreviewFrame {
    BellIcon()
}
