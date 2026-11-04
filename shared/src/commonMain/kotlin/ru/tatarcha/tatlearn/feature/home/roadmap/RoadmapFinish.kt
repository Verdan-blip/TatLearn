package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.Diamond
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.dashedBorder
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.roadmap_finish_next
import tatlearn.shared.generated.resources.roadmap_finish_title

private const val DIMMED = 0.45f
private val Small = 16.dp
private val Large = 22.dp

/** Конец ленты: «Финиш A1», дальше A2 без сроков. Это не кнопка: следующего уровня в приложении пока нет. */
@Composable
fun RoadmapFinish(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs),
    ) {
        Row(
            modifier = Modifier
                .clip(TamyrTheme.shapes.tile)
                .background(TamyrTheme.colors.background)
                .dashedBorder(color = TamyrTheme.colors.contour, shape = TamyrTheme.shapes.tile)
                .padding(horizontal = TamyrTheme.spacing.xl, vertical = TamyrTheme.spacing.m),
            horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Diamond(color = TamyrTheme.colors.accent, size = Small, alpha = DIMMED)
            Diamond(color = TamyrTheme.colors.gold, size = Large, alpha = DIMMED)
            Diamond(color = TamyrTheme.colors.primary, size = Small, alpha = DIMMED)
        }
        Text(
            text = stringResource(Res.string.roadmap_finish_title),
            style = TamyrTheme.typography.body17Bold,
            color = TamyrTheme.colors.textSecondary,
            modifier = Modifier.padding(top = TamyrTheme.spacing.xxs),
        )
        Text(
            text = stringResource(Res.string.roadmap_finish_next),
            style = TamyrTheme.typography.caption13,
            color = TamyrTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun RoadmapFinishPreview() = PreviewFrame {
    RoadmapFinish(modifier = Modifier.padding(TamyrTheme.spacing.screen))
}
