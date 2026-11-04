package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Тень дизайна «0 Ndp 0 color»: жёсткая копия фигуры, сдвинутая вниз, без размытия. */
fun Modifier.solidShadow(color: Color, offsetY: Dp, shape: Shape): Modifier = drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)
    translate(top = offsetY.toPx()) { drawOutline(outline, color) }
}

/** Пунктирная линия 2dp под блоком, например строка ответа в сборке предложения. */
fun Modifier.dashedBottomBorder(color: Color, thickness: Dp = 2.dp, dash: Dp = 6.dp, gap: Dp = 4.dp): Modifier = drawBehind {
    val y = size.height - thickness.toPx() / 2
    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = thickness.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash.toPx(), gap.toPx())),
    )
}

/** Пунктирная рамка по контуру фигуры, например у закрытого модуля на ленте. */
fun Modifier.dashedBorder(color: Color, shape: Shape, width: Dp = 1.5.dp, dash: Dp = 5.dp, gap: Dp = 4.dp): Modifier = drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)
    drawOutline(
        outline = outline,
        color = color,
        style = Stroke(
            width = width.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash.toPx(), gap.toPx())),
        ),
    )
}
