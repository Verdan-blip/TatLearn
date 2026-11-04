package ru.tatarcha.tatlearn.core.designsystem.component.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val ChevronDownIconPaths = listOf(
    "M4.8 9.1 L12 16.3 L19.2 9.1",
)

@Composable
fun ChevronDownIcon(
    modifier: Modifier = Modifier,
    size: Dp = TamyrSizes.chevron,
    tint: Color = TamyrTheme.colors.primary,
) {
    StrokeIcon(
        paths = ChevronDownIconPaths,
        size = size,
        tint = tint,
        modifier = modifier,
        strokeWidth = 2.4f,
    )
}

@Preview
@Composable
private fun ChevronDownIconPreview() = PreviewFrame {
    ChevronDownIcon()
}
