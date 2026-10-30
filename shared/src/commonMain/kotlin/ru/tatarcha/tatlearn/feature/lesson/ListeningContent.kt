package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.audio.AudioPlayerState
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.AnswerOption
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.SlowPlayLink
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.WavePlayButton
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.listening_audio_error
import tatlearn.shared.generated.resources.listening_play_description
import tatlearn.shared.generated.resources.listening_slower
import tatlearn.shared.generated.resources.listening_tag
import tatlearn.shared.generated.resources.listening_title

/** Экран 09. */
@Composable
fun ListeningContent(
    step: ExerciseStep.Listening,
    input: StepInput.Choice,
    feedback: StepFeedback?,
    audio: AudioPlayerState,
    onEvent: (LessonUiEvent) -> Unit,
) {
    val spacing = TamyrTheme.spacing
    val task = step.task
    ExerciseTitle(title = stringResource(Res.string.listening_title), tag = stringResource(Res.string.listening_tag))
    Column(Modifier.padding(horizontal = spacing.screen).padding(top = spacing.l)) {
        TamyrCard(Modifier.fillMaxWidth(), contentPadding = 26.dp) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                WavePlayButton(
                    onClick = { onEvent(LessonUiEvent.PlayAudioClicked()) },
                    contentDescription = stringResource(Res.string.listening_play_description),
                    playing = audio == AudioPlayerState.Playing,
                )
                SlowPlayLink(stringResource(Res.string.listening_slower), onClick = { onEvent(LessonUiEvent.PlayAudioClicked(slow = true)) })
                // TODO(design): состояние ошибки звука не нарисовано, пока простая подпись под кнопкой.
                if (audio == AudioPlayerState.Error) {
                    Text(
                        text = stringResource(Res.string.listening_audio_error),
                        style = TamyrTheme.typography.caption13,
                        color = TamyrTheme.colors.accentText,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
    Column(Modifier.padding(horizontal = spacing.screen, vertical = spacing.xl), verticalArrangement = Arrangement.spacedBy(spacing.m)) {
        task.options.forEachIndexed { index, option ->
            AnswerOption(
                text = option,
                state = choiceState(index, task.correctIndex, input.selected, feedback),
                onClick = { onEvent(LessonUiEvent.OptionSelected(index)) },
                enabled = feedback == null,
                bold = true,
            )
        }
    }
}
