package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrMascot
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.PlayButton
import ru.tatarcha.tatlearn.core.designsystem.component.solidShadow
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import ru.tatarcha.tatlearn.domain.model.WordPair
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.lesson_intro_chip_description
import tatlearn.shared.generated.resources.lesson_intro_play
import tatlearn.shared.generated.resources.lesson_intro_play_description
import tatlearn.shared.generated.resources.lesson_intro_playing
import tatlearn.shared.generated.resources.lesson_intro_summary
import tatlearn.shared.generated.resources.lesson_intro_summary_hint

private val BigPlay = 64.dp
private val ChipPlay = 22.dp

/** Первая в слове заглавная: перевод на карточке всегда с большой буквы (данные хранят слова строчными). */
internal fun String.capitalizedFirst(): String = replaceFirstChar { it.uppercase() }

/**
 * Карточки новых слов (макеты 50–52): одно слово крупно, перевод и звук. На последней карточке снизу сводка чипами со всеми
 * словами группы; по нажатию слово озвучивается. [playingUrl] — запись, которая звучит сейчас.
 */
@Composable
fun IntroWordContent(
    step: ExerciseStep.WordIntro,
    playingUrl: String?,
    onEvent: (LessonUiEvent) -> Unit,
) {
    val word = step.word
    IntroHeader(
        position = step.position,
        total = step.total,
        modifier = Modifier.padding(top = TamyrTheme.spacing.xl),
    )
    TamyrCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = TamyrTheme.spacing.screen, end = TamyrTheme.spacing.screen, top = TamyrTheme.spacing.l),
        contentPadding = TamyrTheme.spacing.screen,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = TamyrTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.l),
        ) {
            Text(
                text = word.tatar,
                style = TamyrTheme.typography.display40,
                color = TamyrTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = word.russian.capitalizedFirst(),
                style = TamyrTheme.typography.body20,
                color = TamyrTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            word.audioUrl?.let { url ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.tight),
                    modifier = Modifier.padding(top = TamyrTheme.spacing.s),
                ) {
                    PlayButton(
                        onClick = { onEvent(LessonUiEvent.PlayWordClicked(url)) },
                        diameter = BigPlay,
                        playing = playingUrl == url,
                        contentDescription = stringResource(Res.string.lesson_intro_play_description),
                    )
                    Text(
                        text = stringResource(if (playingUrl == url) Res.string.lesson_intro_playing else Res.string.lesson_intro_play),
                        style = TamyrTheme.typography.body14Bold,
                        color = TamyrTheme.colors.primary,
                    )
                }
            }
        }
    }
    if (step.isLast) {
        IntroSummary(
            words = step.words,
            playingUrl = playingUrl,
            onEvent = onEvent,
        )
    } else {
        step.note?.let { IntroMascotNote(text = it) }
    }
}

@Composable
private fun IntroMascotNote(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = TamyrTheme.spacing.screen, end = TamyrTheme.spacing.screen, top = TamyrTheme.spacing.l),
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
        verticalAlignment = Alignment.Bottom,
    ) {
        TamyrMascot(pose = MascotPose.Greeting, size = MascotNoteSize)
        Text(
            text = text,
            style = TamyrTheme.typography.body14,
            color = TamyrTheme.colors.textPrimary,
            modifier = Modifier
                .weight(1f)
                .clip(TamyrTheme.shapes.answer)
                .background(TamyrTheme.colors.surface)
                .border(TamyrSizes.borderThin, TamyrTheme.colors.border, TamyrTheme.shapes.answer)
                .padding(horizontal = TamyrTheme.spacing.blockGap, vertical = TamyrTheme.spacing.tight),
        )
    }
}

private val MascotNoteSize = 56.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntroSummary(
    words: List<WordPair>,
    playingUrl: String?,
    onEvent: (LessonUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = TamyrTheme.spacing.screen, end = TamyrTheme.spacing.screen, top = TamyrTheme.spacing.screen),
        verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(Res.string.lesson_intro_summary, words.size),
                style = TamyrTheme.typography.body16Bold,
                color = TamyrTheme.colors.textPrimary,
            )
            Text(
                text = stringResource(Res.string.lesson_intro_summary_hint),
                style = TamyrTheme.typography.caption13,
                color = TamyrTheme.colors.textSecondary,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.tight),
        ) {
            words.forEach { word ->
                IntroWordChip(
                    word = word,
                    playing = word.audioUrl != null && word.audioUrl == playingUrl,
                    onClick = { word.audioUrl?.let { onEvent(LessonUiEvent.PlayWordClicked(it)) } },
                )
            }
        }
    }
}

/** Чип слова на сводке: нажатие озвучивает слово; звучащий чип отмечен зелёной рамкой. */
@Composable
private fun IntroWordChip(
    word: WordPair,
    playing: Boolean,
    onClick: () -> Unit,
) {
    val colors = TamyrTheme.colors
    val description = stringResource(Res.string.lesson_intro_chip_description, word.tatar)
    Row(
        modifier = Modifier
            .heightIn(min = TamyrSizes.topBarAction)
            .solidShadow(if (playing) colors.successBorder else colors.border, TamyrSizes.buttonShadow, TamyrTheme.shapes.chip)
            .clip(TamyrTheme.shapes.chip)
            .background(if (playing) colors.successContainer else colors.surface)
            .border(if (playing) TamyrSizes.borderAccent else TamyrSizes.borderThin, if (playing) colors.primary else colors.border, TamyrTheme.shapes.chip)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(start = TamyrTheme.spacing.s, end = TamyrTheme.spacing.m),
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayButton(
            onClick = onClick,
            diameter = ChipPlay,
            playing = playing,
        )
        Text(
            text = word.tatar,
            style = TamyrTheme.typography.body16Bold,
            color = colors.textPrimary,
        )
    }
}

private val previewWords = listOf(
    WordPair("исәнмесез", "здравствуйте", "audio/isenmesez.m4a"),
    WordPair("исәнме", "привет", "audio/isenme.m4a"),
    WordPair("рәхмәт", "спасибо", "audio/rahmat.m4a"),
)

private fun previewIntro(position: Int, note: String? = "Так здороваются со взрослыми и незнакомыми. Другу можно сказать «Исәнме».") = ExerciseStep.WordIntro(
    id = "intro-${position - 1}",
    word = previewWords[position - 1],
    position = position,
    total = previewWords.size,
    words = previewWords,
    note = note,
)

@Preview(name = "First word")
@Composable
private fun IntroWordFirstPreview() = PreviewFrame {
    Column { IntroWordContent(step = previewIntro(1), playingUrl = null, onEvent = {}) }
}

@Preview(name = "Playing")
@Composable
private fun IntroWordPlayingPreview() = PreviewFrame {
    Column { IntroWordContent(step = previewIntro(2), playingUrl = "audio/isenme.m4a", onEvent = {}) }
}

@Preview(name = "Last word with summary")
@Composable
private fun IntroWordLastPreview() = PreviewFrame {
    Column { IntroWordContent(step = previewIntro(3), playingUrl = "audio/rahmat.m4a", onEvent = {}) }
}
