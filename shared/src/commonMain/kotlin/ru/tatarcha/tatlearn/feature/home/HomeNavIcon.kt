package ru.tatarcha.tatlearn.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

@Composable
fun HomeNavIcon(tint: Color) = Canvas(Modifier.size(TamyrSizes.navIcon)) {
    val w = size.width
    val roof = Path().apply {
        moveTo(w * 0.1f, w * 0.5f); lineTo(w * 0.5f, w * 0.1f); lineTo(w * 0.9f, w * 0.5f)
        lineTo(w * 0.8f, w * 0.5f); lineTo(w * 0.8f, w * 0.9f); lineTo(w * 0.2f, w * 0.9f); lineTo(w * 0.2f, w * 0.5f); close()
    }
    drawPath(roof, tint, style = Fill)
}

@Preview
@Composable
private fun HomeNavIconPreview() = PreviewFrame { HomeNavIcon(TamyrTheme.colors.primary) }
