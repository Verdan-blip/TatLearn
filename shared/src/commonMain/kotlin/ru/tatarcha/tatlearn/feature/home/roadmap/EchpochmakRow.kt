package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.icon.EchpochmakIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val SparkDistance = 10.dp
private val SparkHalf = 2.5.dp
private val SparkAngles = listOf(-90f, 30f, 150f)

/**
 * Прогресс модуля: по эчпочмаку на каждый урок, пройденные золотые.
 * [litIndex] и [animation]: эчпочмак с этим номером «румянится» (заливка, всплеск и искры), пока играет анимация урока.
 */
@Composable
fun EchpochmakRow(
    done: Int,
    total: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = TamyrSizes.echpochmak,
    litIndex: Int? = null,
    animation: RoadmapAnimation? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            if (animation != null && index == litIndex) {
                LitEchpochmak(animation = animation, size = iconSize)
            } else {
                EchpochmakIcon(
                    filled = index < done,
                    size = iconSize,
                )
            }
        }
    }
}

@Composable
private fun LitEchpochmak(
    animation: RoadmapAnimation,
    size: Dp,
) {
    val spark = TamyrTheme.colors.gold
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = animation.pulse.value
                scaleY = animation.pulse.value
            }
            .drawBehind {
                val progress = animation.sparks.value
                if (progress > 0f && progress < 1f) {
                    val distance = SparkDistance.toPx() * progress
                    SparkAngles.forEach { degrees ->
                        val radians = degrees * (PI.toFloat() / 180f)
                        val center = Offset(this.size.width / 2 + cos(radians) * (this.size.width / 2 + distance), this.size.height / 2 + sin(radians) * (this.size.height / 2 + distance))
                        drawTrailDiamond(
                            center = center,
                            half = SparkHalf.toPx(),
                            passed = true,
                            filledColor = spark.copy(alpha = 1f - progress),
                            outlineColor = spark,
                        )
                    }
                }
            },
    ) {
        EchpochmakIcon(
            filled = false,
            size = size,
            fill = animation.eggFill.value,
        )
    }
}

@Preview
@Composable
private fun EchpochmakRowPreview() = PreviewFrame {
    EchpochmakRow(
        done = 3,
        total = 5,
    )
}
