package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val DiamondStep = 15.dp

/**
 * Тропа из ромбов между рядами ленты: плавная S-кривая от [from] к [to]. Пройденный участок золотой, будущий контурный.
 * Тропа это декор: без нажатий и не читается скринридером.
 */
@Composable
fun RoadmapConnector(
    from: TrailSide,
    to: TrailSide,
    passed: Boolean,
    modifier: Modifier = Modifier,
) {
    val filled = TamyrTheme.colors.gold
    val outline = TamyrTheme.colors.contour
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(TamyrSizes.roadmapConnector),
    ) {
        val startX = size.width * from.fraction
        val endX = size.width * to.fraction
        val path = Path().apply {
            moveTo(startX, 0f)
            cubicTo(startX, size.height * 0.55f, endX, size.height * 0.45f, endX, size.height)
        }
        val measure = PathMeasure().apply { setPath(path, false) }
        val step = DiamondStep.toPx()
        var distance = step / 2
        while (distance < measure.length) {
            drawTrailDiamond(
                center = measure.getPosition(distance),
                half = TamyrSizes.roadmapDiamond.toPx() / 2,
                passed = passed,
                filledColor = filled,
                outlineColor = outline,
            )
            distance += step
        }
    }
}

@Preview(name = "Passed, right to left")
@Composable
private fun RoadmapConnectorPassedPreview() = PreviewFrame {
    RoadmapConnector(
        from = TrailSide.Right,
        to = TrailSide.Left,
        passed = true,
    )
}

@Preview(name = "Ahead, straight")
@Composable
private fun RoadmapConnectorAheadPreview() = PreviewFrame {
    RoadmapConnector(
        from = TrailSide.Left,
        to = TrailSide.Left,
        passed = false,
    )
}
