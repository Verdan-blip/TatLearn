package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.icon.EchpochmakIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Прогресс модуля: по эчпочмаку на каждый урок, пройденные золотые. */
@Composable
fun EchpochmakRow(
    done: Int,
    total: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = TamyrSizes.echpochmak,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            EchpochmakIcon(
                filled = index < done,
                size = iconSize,
            )
        }
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
