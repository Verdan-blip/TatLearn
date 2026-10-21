package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTagKind
import ru.tatarcha.tatlearn.core.designsystem.component.dashedBottomBorder
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.WordChip
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.WordChipState
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.sentence_prompt_format
import tatlearn.shared.generated.resources.sentence_tag
import tatlearn.shared.generated.resources.sentence_title

private val AnswerLineMinHeight = 58.dp

/** Экран 10: нажимайте слова из банка, чтобы собрать перевод; нажмите выставленное слово, чтобы вернуть его. */
@Composable
fun SentenceContent(step: ExerciseStep.SentenceBuilder, input: StepInput.Sentence, feedback: StepFeedback?, onEvent: (LessonUiEvent) -> Unit) {
    val spacing = TamyrTheme.spacing
    val colors = TamyrTheme.colors
    val task = step.task
    ExerciseTitle(
        title = stringResource(Res.string.sentence_title),
        tag = stringResource(Res.string.sentence_tag),
        tagKind = TamyrTagKind.Error,
    )
    Column(Modifier.padding(horizontal = spacing.screen).padding(top = spacing.l), verticalArrangement = Arrangement.spacedBy(spacing.l)) {
        TamyrCard(Modifier.fillMaxWidth(), contentPadding = 18.dp) {
            Text(stringResource(Res.string.sentence_prompt_format, task.promptRu), style = TamyrTheme.typography.title19, color = colors.textPrimary)
        }
        Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
            FlowRow(
                Modifier.fillMaxWidth().heightIn(min = AnswerLineMinHeight).dashedBottomBorder(colors.dashedLine).padding(horizontal = spacing.xxs, vertical = spacing.s),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                input.placed.forEachIndexed { position, bankIndex ->
                    WordChip(task.wordBank[bankIndex], WordChipState.Placed, onClick = { if (feedback == null) onEvent(LessonUiEvent.PlacedWordClicked(position)) })
                }
            }
            // В макете для длинных предложений показана вторая, пустая строка.
            Column(Modifier.fillMaxWidth().heightIn(min = AnswerLineMinHeight).dashedBottomBorder(colors.dashedLine)) {}
        }
    }
    FlowRow(
        Modifier.fillMaxWidth().padding(horizontal = spacing.screen, vertical = 26.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        task.wordBank.forEachIndexed { bankIndex, word ->
            val used = bankIndex in input.placed
            WordChip(
                word,
                if (used) WordChipState.Used else WordChipState.Available,
                onClick = { if (!used && feedback == null) onEvent(LessonUiEvent.BankWordClicked(bankIndex)) },
            )
        }
    }
}
