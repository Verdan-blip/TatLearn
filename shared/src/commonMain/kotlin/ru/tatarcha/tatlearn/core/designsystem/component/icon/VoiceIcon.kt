package ru.tatarcha.tatlearn.core.designsystem.component.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val VoiceIconPaths = listOf(
    "M12 3 A3 3 0 0 1 15 6 V11 A3 3 0 0 1 9 11 V6 A3 3 0 0 1 12 3 Z",
            "M5.5 11 Q5.5 17 12 17 Q18.5 17 18.5 11",
            "M12 17 V21",
)

@Composable
fun VoiceIcon(
    modifier: Modifier = Modifier,
    size: Dp = TamyrSizes.settingsIcon,
    tint: Color = TamyrTheme.colors.primary,
) {
    StrokeIcon(
        paths = VoiceIconPaths,
        size = size,
        tint = tint,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun VoiceIconPreview() = PreviewFrame {
    VoiceIcon()
}
