package ru.tatarcha.tatlearn.core.designsystem.component.icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp

private const val VIEWPORT = 24f

@Composable
internal fun StrokeIcon(
    paths: List<String>,
    size: Dp,
    tint: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 2.2f,
) {
    val parsed = remember(paths) { paths.map { PathParser().parsePathString(it).toPath() } }
    Canvas(modifier = modifier.size(size)) {
        val factor = this.size.width / VIEWPORT
        withTransform({ scale(scaleX = factor, scaleY = factor, pivot = Offset.Zero) }) {
            parsed.forEach { path ->
                drawPath(
                    path = path,
                    color = tint,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }
    }
}
