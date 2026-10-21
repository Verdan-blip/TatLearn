package ru.tatarcha.tatlearn.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val DotActiveWidth = 22.dp
private val DotSize = 6.dp

@Composable
fun PagerDots(
    current: Int,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xs),
    ) {
        repeat(count) { index ->
            val isActive = index == current

            Box(
                modifier = Modifier
                    .width(if (isActive) DotActiveWidth else DotSize)
                    .height(DotSize)
                    .clip(CircleShape)
                    .background(
                        color = if (isActive) TamyrTheme.colors.primary else TamyrTheme.colors.borderStrong,
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun PagerDotsFirstPreview() = PreviewFrame {
    PagerDots(
        current = 0,
        count = 3,
    )
}

@Preview
@Composable
private fun PagerDotsLastPreview() = PreviewFrame {
    PagerDots(
        current = 2,
        count = 3,
    )
}
