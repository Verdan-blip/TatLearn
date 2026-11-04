package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private const val DIAMONDS = 4
private val LinkWidth = 62.dp
private val LinkHeight = 12.dp

/** Короткая тропа между двумя плитками одного ряда; тропа это декор и не нажимается. */
@Composable
fun RoadmapTileLink(
    passed: Boolean,
    modifier: Modifier = Modifier,
) {
    val filled = TamyrTheme.colors.gold
    val outline = TamyrTheme.colors.contour
    Canvas(modifier = modifier.size(DpSize(LinkWidth, LinkHeight))) {
        val step = size.width / (DIAMONDS - 1)
        repeat(DIAMONDS) { index ->
            drawTrailDiamond(
                center = Offset(index * step, size.height / 2),
                half = 3.5.dp.toPx(),
                passed = passed,
                filledColor = filled,
                outlineColor = outline,
            )
        }
    }
}

@Preview
@Composable
private fun RoadmapTileLinkPreview() = PreviewFrame {
    RoadmapTileLink(passed = true)
}
