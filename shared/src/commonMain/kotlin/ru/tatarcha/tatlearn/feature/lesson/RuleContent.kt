package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrMascot
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.PlayButton
import ru.tatarcha.tatlearn.core.designsystem.component.icon.LightbulbIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import ru.tatarcha.tatlearn.domain.model.LessonRule
import ru.tatarcha.tatlearn.domain.model.RuleExample
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.lesson_rule_example_description
import tatlearn.shared.generated.resources.lesson_rule_hint
import tatlearn.shared.generated.resources.lesson_rule_tag

private val StripWidth = 6.dp
private val ExamplePlay = 48.dp
private val TeacherSize = 64.dp

/**
 * Карточка-правило (макет 53): золотая метка с лампочкой, зелёная полоса у карточки, одна мысль и примеры со звуком.
 * Окончание в примере подчёркнуто. Содержимое правил: needs native-speaker review.
 */
@Composable
fun RuleContent(
    step: ExerciseStep.Rule,
    playingUrl: String?,
    onEvent: (LessonUiEvent) -> Unit,
) {
    val rule = step.rule
    RuleTag(modifier = Modifier.padding(start = TamyrTheme.spacing.screen, top = TamyrTheme.spacing.xl))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(start = TamyrTheme.spacing.screen, end = TamyrTheme.spacing.screen, top = TamyrTheme.spacing.l)
            .clip(TamyrTheme.shapes.card)
            .background(TamyrTheme.colors.surface)
            .border(TamyrSizes.borderThin, TamyrTheme.colors.border, TamyrTheme.shapes.card),
    ) {
        Box(
            modifier = Modifier
                .width(StripWidth)
                .fillMaxHeight()
                .background(TamyrTheme.colors.primary),
        )
        Column(
            modifier = Modifier.padding(start = TamyrTheme.spacing.xl, end = TamyrTheme.spacing.xl, top = TamyrTheme.spacing.xl, bottom = TamyrTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.section),
        ) {
            Text(
                text = rule.title,
                style = TamyrTheme.typography.title22,
                color = TamyrTheme.colors.textPrimary,
            )
            Text(
                text = rule.text,
                style = TamyrTheme.typography.body16,
                color = TamyrTheme.colors.textPrimary,
            )
            Box(Modifier.fillMaxWidth().height(TamyrSizes.borderThin).background(TamyrTheme.colors.border))
            rule.examples.forEach { example ->
                RuleExampleRow(
                    example = example,
                    playing = example.audioUrl != null && example.audioUrl == playingUrl,
                    onPlay = { example.audioUrl?.let { onEvent(LessonUiEvent.PlayWordClicked(it)) } },
                )
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = TamyrTheme.spacing.screen, end = TamyrTheme.spacing.screen, top = TamyrTheme.spacing.l),
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = stringResource(Res.string.lesson_rule_hint),
            style = TamyrTheme.typography.body14,
            color = TamyrTheme.colors.textPrimary,
            modifier = Modifier
                .weight(1f)
                .clip(TamyrTheme.shapes.answer)
                .background(TamyrTheme.colors.surface)
                .border(TamyrSizes.borderThin, TamyrTheme.colors.border, TamyrTheme.shapes.answer)
                .padding(horizontal = TamyrTheme.spacing.blockGap, vertical = TamyrTheme.spacing.tight),
        )
        TamyrMascot(pose = MascotPose.Teacher, size = TeacherSize)
    }
}

@Composable
private fun RuleTag(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(TamyrTheme.shapes.tag)
            .background(TamyrTheme.colors.goldContainer)
            .padding(horizontal = TamyrTheme.spacing.tight, vertical = TamyrTheme.spacing.xs - 1.dp),
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LightbulbIcon(tint = TamyrTheme.colors.goldText)
        Text(
            text = stringResource(Res.string.lesson_rule_tag).uppercase(),
            style = TamyrTheme.typography.tag,
            color = TamyrTheme.colors.goldText,
        )
    }
}

@Composable
private fun RuleExampleRow(
    example: RuleExample,
    playing: Boolean,
    onPlay: () -> Unit,
) {
    val description = stringResource(Res.string.lesson_rule_example_description, example.tatar.trimEnd('.'))
    Row(
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.blockGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (example.audioUrl != null) {
            PlayButton(
                onClick = onPlay,
                diameter = ExamplePlay,
                playing = playing,
                contentDescription = description,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs / 2)) {
            Text(
                text = highlighted(example.tatar, example.highlight, TamyrTheme.colors.primary),
                style = TamyrTheme.typography.title19,
                color = TamyrTheme.colors.textPrimary,
            )
            Text(
                text = example.russian,
                style = TamyrTheme.typography.body15,
                color = TamyrTheme.colors.textSecondary,
            )
        }
    }
}

/** Подчёркивает последнее вхождение [highlight] (окончание обычно стоит в конце слова); без [highlight] текст не меняется. */
private fun highlighted(text: String, highlight: String?, color: Color) = buildAnnotatedString {
    val at = highlight?.let { text.trimEnd('.', '!', '?').lastIndexOf(it) } ?: -1
    if (highlight == null || at < 0) {
        append(text)
    } else {
        append(text.substring(0, at))
        withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) { append(highlight) }
        append(text.substring(at + highlight.length))
    }
}

private val previewRule = LessonRule(
    title = "«Я» прячется в конце глагола",
    text = "Окончание -м после основы глагола значит «я»: чәй эчәм, «я пью чай».",
    examples = listOf(
        RuleExample("Мин чәй эчәм.", "Я пью чай.", "м", "audio/min_chay_echam.m4a"),
        RuleExample("Мин алма ашыйм.", "Я ем яблоко.", "м", "audio/min_alma_ashyym.m4a"),
    ),
)

@Preview
@Composable
private fun RuleContentPreview() = PreviewFrame {
    Column { RuleContent(step = ExerciseStep.Rule("rule", previewRule), playingUrl = null, onEvent = {}) }
}
