package ru.tatarcha.tatlearn.core.designsystem.component.icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

@Composable
fun ChevronBackIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = TamyrTheme.colors.textSecondary,
) {
    Canvas(modifier = modifier.size(size)) {
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.62f, this@Canvas.size.height * 0.2f)
            lineTo(this@Canvas.size.width * 0.32f, this@Canvas.size.height * 0.5f)
            lineTo(this@Canvas.size.width * 0.62f, this@Canvas.size.height * 0.8f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = size.toPx() * 0.1f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

@Preview
@Composable
private fun ChevronBackIconPreview() = PreviewFrame {
    ChevronBackIcon()
}
