package ru.tatarcha.tatlearn.core.designsystem.component.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val ChevronForwardIconPaths = listOf(
    "M9.1 4.8 L16.3 12 L9.1 19.2",
)

@Composable
fun ChevronForwardIcon(
    modifier: Modifier = Modifier,
    size: Dp = TamyrSizes.chevron,
    tint: Color = TamyrTheme.colors.primary,
) {
    StrokeIcon(
        paths = ChevronForwardIconPaths,
        size = size,
        tint = tint,
        modifier = modifier,
        strokeWidth = 2.4f,
    )
}

@Preview
@Composable
private fun ChevronForwardIconPreview() = PreviewFrame {
    ChevronForwardIcon()
}
