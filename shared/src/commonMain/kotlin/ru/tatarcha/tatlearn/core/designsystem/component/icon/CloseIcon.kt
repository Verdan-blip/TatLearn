package ru.tatarcha.tatlearn.core.designsystem.component.icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

@Composable
fun CloseIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = TamyrTheme.colors.textSecondary,
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = size.toPx() * 0.1f
        drawLine(
            color = tint,
            start = Offset(this.size.width * 0.25f, this.size.height * 0.25f),
            end = Offset(this.size.width * 0.75f, this.size.height * 0.75f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(this.size.width * 0.75f, this.size.height * 0.25f),
            end = Offset(this.size.width * 0.25f, this.size.height * 0.75f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Preview
@Composable
private fun CloseIconPreview() = PreviewFrame {
    CloseIcon()
}
