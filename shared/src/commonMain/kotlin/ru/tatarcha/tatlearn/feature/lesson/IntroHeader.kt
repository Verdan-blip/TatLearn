package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTag
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTagKind
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.lesson_intro_counter
import tatlearn.shared.generated.resources.lesson_intro_counter_description
import tatlearn.shared.generated.resources.lesson_intro_tag

private val CurrentDiamond = 12.dp
private val OtherDiamond = 9.dp

/** Верх карточки нового слова: золотая метка «Новое слово» и пять ромбов «слово N из M» (пройденные зелёные, текущий золотой). */
@Composable
fun IntroHeader(
    position: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(Res.string.lesson_intro_counter_description, position, total)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = TamyrTheme.spacing.screen),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TamyrTag(
            text = stringResource(Res.string.lesson_intro_tag),
            kind = TamyrTagKind.Gold,
        )
        Row(
            modifier = Modifier.semantics { contentDescription = description },
            horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.clearAndSetSemantics { },
                horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(total) { index -> CounterDiamond(index + 1, position) }
            }
            Text(
                text = stringResource(Res.string.lesson_intro_counter, position, total),
                style = TamyrTheme.typography.caption13Bold,
                color = TamyrTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun CounterDiamond(
    number: Int,
    position: Int,
) {
    val current = number == position
    val colors = TamyrTheme.colors
    Box(
        modifier = Modifier
            .size(if (current) CurrentDiamond else OtherDiamond)
            .graphicsLayer { rotationZ = 45f }
            .then(
                when {
                    current -> Modifier.background(colors.gold).border(1.5.dp, colors.goldText)
                    number < position -> Modifier.background(colors.primary)
                    else -> Modifier.border(1.5.dp, colors.contour)
                },
            ),
    )
}

@Preview
@Composable
private fun IntroHeaderPreview() = PreviewFrame {
    IntroHeader(
        position = 3,
        total = 5,
    )
}
