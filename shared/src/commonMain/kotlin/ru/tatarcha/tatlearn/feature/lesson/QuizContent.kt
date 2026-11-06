package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.audio.AudioPlayerState
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTagKind
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.AnswerOption
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.AnswerOptionState
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.PlayButton
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.lesson_retry_hint
import tatlearn.shared.generated.resources.lesson_retry_tag
import tatlearn.shared.generated.resources.listening_play_description
import tatlearn.shared.generated.resources.quiz_tag_new_word
import tatlearn.shared.generated.resources.quiz_title

/** Экран 05. */
@Composable
fun QuizContent(
    step: ExerciseStep.Quiz,
    input: StepInput.Choice,
    feedback: StepFeedback?,
    audio: AudioPlayerState,
    onEvent: (LessonUiEvent) -> Unit,
) {
    val q = step.question
    val spacing = TamyrTheme.spacing
    ExerciseTitle(
        title = stringResource(Res.string.quiz_title),
        tag = when {
            step.isRetry -> stringResource(Res.string.lesson_retry_tag)
            q.isNewWord -> stringResource(Res.string.quiz_tag_new_word)
            else -> null
        },
        tagKind = if (step.isRetry) TamyrTagKind.Gold else TamyrTagKind.Error,
        subtitle = if (step.isRetry) stringResource(Res.string.lesson_retry_hint) else null,
    )
    Column(Modifier.padding(horizontal = spacing.screen).padding(top = spacing.l)) {
        TamyrCard(Modifier.fillMaxWidth(), contentPadding = 22.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.l), verticalAlignment = Alignment.CenterVertically) {
                // Кнопка звука только когда у слова есть запись: без озвучки она бы ничего не играла (TODO(audio)).
                if (q.audioUrl != null) {
                    PlayButton(
                        onClick = { onEvent(LessonUiEvent.PlayAudioClicked()) },
                        playing = audio == AudioPlayerState.Playing,
                        contentDescription = stringResource(Res.string.listening_play_description),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(q.word, style = TamyrTheme.typography.display30, color = TamyrTheme.colors.textPrimary)
                    q.transcription?.let { Text(it, style = TamyrTheme.typography.transcription.copy(fontSize = TamyrTheme.typography.body14.fontSize), color = TamyrTheme.colors.textSecondary) }
                }
            }
        }
    }
    Column(Modifier.padding(horizontal = spacing.screen, vertical = spacing.xl), verticalArrangement = Arrangement.spacedBy(spacing.m)) {
        q.options.forEachIndexed { index, option ->
            AnswerOption(
                text = option,
                state = choiceState(index, q.correctIndex, input.selected, feedback),
                onClick = { onEvent(LessonUiEvent.OptionSelected(index)) },
                enabled = feedback == null,
                badge = (index + 1).toString(),
            )
        }
    }
}

/** Общее для квиза и аудирования: как выглядит вариант до и после проверки. */
internal fun choiceState(index: Int, correctIndex: Int, selected: Int?, feedback: StepFeedback?): AnswerOptionState = when {
    feedback == null -> if (index == selected) AnswerOptionState.Selected else AnswerOptionState.Neutral
    index == correctIndex -> AnswerOptionState.Correct
    index == selected -> AnswerOptionState.Wrong
    else -> AnswerOptionState.Neutral
}
