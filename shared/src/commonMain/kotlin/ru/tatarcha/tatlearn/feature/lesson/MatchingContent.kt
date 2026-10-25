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
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrMascot
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.AnswerOption
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.AnswerOptionState
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.matching_subtitle
import tatlearn.shared.generated.resources.matching_title

private val TileHeight = 58.dp
private val HintMascotSize = 48.dp
private val ColumnGap = 14.dp

/**
 * Экран 04. Левая колонка: татарские слова в порядке урока, правая: переводы в фиксированном перемешивании.
 * Нажмите татарское слово, затем его перевод; верная пара становится плоской зелёной плиткой без значка и больше не нажимается,
 * неверная на мгновение подсвечивается терракотой с ✕ и покачивается, затем выбор снимается (жизней нет). Пока выбрано слово,
 * не найденные переводы чуть «приподняты» тенью.
 */
@Composable
fun MatchingContent(step: ExerciseStep.Matching, input: StepInput.Matching, onEvent: (LessonUiEvent) -> Unit) {
    val spacing = TamyrTheme.spacing
    ExerciseTitle(title = stringResource(Res.string.matching_title), subtitle = stringResource(Res.string.matching_subtitle))
    Column(
        Modifier.padding(horizontal = spacing.screen, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        step.pairs.indices.forEach { row ->
            val leftIndex = row
            val rightIndex = step.rightOrder[row]
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ColumnGap)) {
                AnswerOption(
                    text = step.pairs[leftIndex].tatar,
                    state = tileState(leftIndex, input, isLeft = true),
                    onClick = { onEvent(LessonUiEvent.MatchLeftClicked(leftIndex)) },
                    modifier = Modifier.weight(1f),
                    bold = true,
                    centered = true,
                    height = TileHeight,
                    shakeOnWrong = true,
                )
                val rightState = tileState(rightIndex, input, isLeft = false)
                AnswerOption(
                    text = step.pairs[rightIndex].russian,
                    state = rightState,
                    onClick = { onEvent(LessonUiEvent.MatchRightClicked(rightIndex)) },
                    modifier = Modifier.weight(1f),
                    centered = true,
                    height = TileHeight,
                    lifted = input.leftSelected != null && rightState == AnswerOptionState.Neutral,
                    shakeOnWrong = true,
                )
            }
        }
    }
    val note = input.leftSelected?.let { step.notes[step.pairs[it].tatar] }
    if (note != null) {
        TamyrCard(Modifier.padding(horizontal = spacing.screen).fillMaxWidth(), shape = TamyrTheme.shapes.answer, contentPadding = 12.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.m), verticalAlignment = Alignment.CenterVertically) {
                TamyrMascot(MascotPose.Teacher, HintMascotSize)
                Text(note, style = TamyrTheme.typography.caption13.copy(lineHeight = TamyrTheme.typography.caption13.fontSize * 1.4f), color = TamyrTheme.colors.textSecondary)
            }
        }
    }
}

private fun tileState(pairIndex: Int, input: StepInput.Matching, isLeft: Boolean): AnswerOptionState {
    val wrong = input.wrongPair
    return when {
        pairIndex in input.matched -> AnswerOptionState.Matched
        wrong != null && ((isLeft && wrong.first == pairIndex) || (!isLeft && wrong.second == pairIndex)) -> AnswerOptionState.Wrong
        isLeft && input.leftSelected == pairIndex -> AnswerOptionState.Selected
        else -> AnswerOptionState.Neutral
    }
}
