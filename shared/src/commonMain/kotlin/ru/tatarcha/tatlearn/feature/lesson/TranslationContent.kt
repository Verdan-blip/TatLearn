package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.audio.AudioPlayerState
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTagKind
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTextField
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.PlayButton
import ru.tatarcha.tatlearn.core.designsystem.component.solidShadow
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.listening_play_description
import tatlearn.shared.generated.resources.sentence_prompt_format
import tatlearn.shared.generated.resources.translation_caption
import tatlearn.shared.generated.resources.translation_letter_description
import tatlearn.shared.generated.resources.translation_letters
import tatlearn.shared.generated.resources.translation_tag
import tatlearn.shared.generated.resources.translation_title

private val InputMinHeight = 56.dp
private val LetterKeySize = 48.dp
private val PlayButtonSize = 40.dp

/**
 * Экран 11. Свободный ввод плюс ряд татарских букв, которые вставляются в позицию курсора. Ответы сравниваются
 * через AnswerNormalizer, поэтому ә/ө/ү/җ/ң/һ, набранные как а/о/у/ж/н/х на русской клавиатуре, всё равно принимаются.
 */
@Composable
fun TranslationContent(
    step: ExerciseStep.Translation,
    input: StepInput.Translation,
    feedback: StepFeedback?,
    audio: AudioPlayerState,
    onEvent: (LessonUiEvent) -> Unit,
) {
    val spacing = TamyrTheme.spacing
    val colors = TamyrTheme.colors
    val letters = stringResource(Res.string.translation_letters).split(' ').filter { it.isNotEmpty() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    // После проверки поле заблокировано, а панель ответа не должна оказаться под клавиатурой.
    LaunchedEffect(feedback != null) { if (feedback != null) dismissKeyboard(focusManager, keyboard) }
    ExerciseTitle(
        title = stringResource(Res.string.translation_title),
        tag = stringResource(Res.string.translation_tag),
        tagKind = TamyrTagKind.Gold,
    )
    Column(Modifier.padding(horizontal = spacing.screen).padding(top = spacing.l), verticalArrangement = Arrangement.spacedBy(spacing.l)) {
        TamyrCard(Modifier.fillMaxWidth(), contentPadding = 18.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                // Кнопка звука только когда у задания есть запись (TODO(audio)).
                if (step.task.audioUrl != null) {
                    PlayButton(
                        onClick = { onEvent(LessonUiEvent.PlayAudioClicked()) },
                        diameter = PlayButtonSize,
                        playing = audio == AudioPlayerState.Playing,
                        contentDescription = stringResource(Res.string.listening_play_description),
                    )
                }
                Text(stringResource(Res.string.sentence_prompt_format, step.task.promptRu), style = TamyrTheme.typography.title19, color = colors.textPrimary)
            }
        }
        TamyrTextField(
            value = input.value,
            onValueChange = { if (feedback == null) onEvent(LessonUiEvent.TextChanged(it)) },
            singleLine = true,
            minHeight = InputMinHeight,
            textStyle = TamyrTheme.typography.body17.copy(fontSize = TamyrTheme.typography.title19.fontSize),
            // Ответ — 1-3 слова: «Готово» закрывает клавиатуру и проверяет ответ (пустой — только закрывает).
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    dismissKeyboard(focusManager, keyboard)
                    if (feedback == null && input.value.text.isNotBlank()) onEvent(LessonUiEvent.CheckClicked)
                },
            ),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.s, Alignment.CenterHorizontally)) {
            letters.forEach { letter ->
                LetterKey(letter, enabled = feedback == null) { onEvent(LessonUiEvent.LetterClicked(letter)) }
            }
        }
        Text(
            stringResource(Res.string.translation_caption),
            style = TamyrTheme.typography.caption13,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Сворачивает клавиатуру; на iOS одного сброса фокуса может не хватить, поэтому прячем и явно. */
internal fun dismissKeyboard(focusManager: FocusManager, keyboard: SoftwareKeyboardController?) {
    focusManager.clearFocus()
    keyboard?.hide()
}

@Composable
private fun LetterKey(letter: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = TamyrTheme.colors
    val shape = TamyrTheme.shapes.chip
    val description = stringResource(Res.string.translation_letter_description, letter)
    Box(
        Modifier
            .semantics { contentDescription = description }
            .solidShadow(colors.border, TamyrSizes.buttonShadow, shape)
            .size(LetterKeySize)
            .clip(shape)
            .background(colors.surface)
            .border(TamyrSizes.borderThin, colors.border, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(letter, style = TamyrTheme.typography.body17Bold, color = colors.textPrimary)
    }
}
