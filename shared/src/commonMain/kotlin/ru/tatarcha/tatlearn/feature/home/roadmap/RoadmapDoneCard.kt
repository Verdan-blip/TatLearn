package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrMascot
import ru.tatarcha.tatlearn.core.designsystem.component.solidShadow
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import androidx.compose.ui.unit.dp
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.roadmap_done_button
import tatlearn.shared.generated.resources.roadmap_done_message
import tatlearn.shared.generated.resources.roadmap_done_title

private val CardShadow = 5.dp

/** Курс пройден (макет 48): вместо карточки текущего модуля праздничная карточка и «Повторить слова». */
@Composable
fun RoadmapDoneCard(
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .solidShadow(TamyrTheme.colors.gold, CardShadow, TamyrTheme.shapes.roadmapCard)
            .clip(TamyrTheme.shapes.roadmapCard)
            .background(TamyrTheme.colors.surface)
            .border(TamyrSizes.borderAccent, TamyrTheme.colors.gold, TamyrTheme.shapes.roadmapCard),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TamyrSizes.roadmapBand + TamyrTheme.spacing.xl)
                .background(TamyrTheme.colors.goldContainer),
            contentAlignment = Alignment.Center,
        ) {
            TamyrMascot(
                pose = MascotPose.Cheer,
                size = TamyrSizes.resultMascotCompact,
            )
        }
        Column(
            modifier = Modifier.padding(TamyrTheme.spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
        ) {
            Text(
                text = stringResource(Res.string.roadmap_done_title),
                style = TamyrTheme.typography.title22,
                color = TamyrTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.roadmap_done_message),
                style = TamyrTheme.typography.body14,
                color = TamyrTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            TamyrButton(
                text = stringResource(Res.string.roadmap_done_button),
                onClick = onRepeat,
                modifier = Modifier.padding(top = TamyrTheme.spacing.s),
            )
        }
    }
}

@Preview
@Composable
private fun RoadmapDoneCardPreview() = PreviewFrame {
    RoadmapDoneCard(
        onRepeat = {},
        modifier = Modifier.padding(TamyrTheme.spacing.screen),
    )
}
