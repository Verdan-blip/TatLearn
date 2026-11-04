package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val DiamondStep = 15.dp
private const val PULSE_COUNT = 3
private const val PULSE_CYCLE_MS = 1_200
private const val PULSE_PHASE_STEP = 0.1667f
private const val PULSE_MIN_ALPHA = 0.35f
private const val PULSE_MIN_SCALE = 0.85f
private const val PULSE_MAX_SCALE = 1.1f

/**
 * Тропа из ромбов между рядами ленты: плавная S-кривая от [from] к [to]. Пройденный участок золотой, будущий контурный.
 * [reveal] (0..1) показывает, какая доля ромбов уже золотая, пока тропа заливается после модуля;
 * [pulsing] включает «волну к цели» на последних трёх ромбах перед карточкой текущего модуля.
 * Тропа это декор: без нажатий и не читается скринридером.
 */
@Composable
fun RoadmapConnector(
    from: TrailSide,
    to: TrailSide,
    passed: Boolean,
    modifier: Modifier = Modifier,
    reveal: () -> Float = { 1f },
    pulsing: Boolean = false,
) {
    val filled = TamyrTheme.colors.gold
    val outline = TamyrTheme.colors.contour
    val cycle: State<Float>? = if (pulsing) rememberPulseCycle() else null
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
        val count = ((measure.length - step / 2) / step).toInt() + 1
        val revealed = reveal()
        var index = 0
        var distance = step / 2
        while (distance < measure.length) {
            val isGold = passed && (index + 0.5f) / count <= revealed
            val fromEnd = count - 1 - index
            val pulse = if (cycle != null && fromEnd < PULSE_COUNT) pulseValue(cycle.value, PULSE_COUNT - 1 - fromEnd) else 1f
            val center = measure.getPosition(distance)
            val half = TamyrSizes.roadmapDiamond.toPx() / 2
            if (pulse < 1f || cycle != null) {
                val scale = PULSE_MIN_SCALE + (PULSE_MAX_SCALE - PULSE_MIN_SCALE) * pulse
                scale(scale = scale, pivot = center) {
                    drawTrailDiamond(center = center, half = half, passed = isGold, filledColor = filled.copy(alpha = pulseAlpha(pulse)), outlineColor = outline.copy(alpha = pulseAlpha(pulse)))
                }
            } else {
                drawTrailDiamond(center = center, half = half, passed = isGold, filledColor = filled, outlineColor = outline)
            }
            distance += step
            index++
        }
    }
}

/** Тройка ромбов пульсирует волной: каждый следующий отстаёт по фазе, треугольная волна. */
private fun pulseValue(cycle: Float, order: Int): Float {
    val phase = ((cycle - order * PULSE_PHASE_STEP) % 1f + 1f) % 1f
    return 1f - abs(2f * phase - 1f)
}

private fun pulseAlpha(pulse: Float): Float = PULSE_MIN_ALPHA + (1f - PULSE_MIN_ALPHA) * pulse

@Composable
private fun rememberPulseCycle(): State<Float> = rememberInfiniteTransition(label = "trail-pulse").animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(PULSE_CYCLE_MS, easing = LinearEasing), RepeatMode.Restart),
    label = "trail-pulse-cycle",
)

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

@Preview(name = "Half revealed")
@Composable
private fun RoadmapConnectorRevealPreview() = PreviewFrame {
    RoadmapConnector(
        from = TrailSide.Left,
        to = TrailSide.Right,
        passed = true,
        reveal = { 0.5f },
    )
}
