package ru.tatarcha.tatlearn.core.designsystem.component.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val SoundIconPaths = listOf(
    "M4 9 V15 H8 L13 19 V5 L8 9 Z",
            "M16.5 9 Q18.5 12 16.5 15",
            "M19 6.5 Q22.5 12 19 17.5",
)

@Composable
fun SoundIcon(
    modifier: Modifier = Modifier,
    size: Dp = TamyrSizes.settingsIcon,
    tint: Color = TamyrTheme.colors.primary,
) {
    StrokeIcon(
        paths = SoundIconPaths,
        size = size,
        tint = tint,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun SoundIconPreview() = PreviewFrame {
    SoundIcon()
}
