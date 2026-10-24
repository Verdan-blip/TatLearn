package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.ui.text.input.TextFieldValue
import ru.tatarcha.tatlearn.core.audio.AudioPlayerState
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.common.UiEffect
import ru.tatarcha.tatlearn.core.common.UiEvent
import ru.tatarcha.tatlearn.core.common.UiState
import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import ru.tatarcha.tatlearn.domain.exercise.XpRules
import ru.tatarcha.tatlearn.navigation.ResultKey

/** Что пользователь уже ввёл на текущем шаге; по одному варианту на каждый вид упражнения. */
sealed interface StepInput {
    /** Квиз и аудирование: индекс выбранного варианта. */
    data class Choice(val selected: Int? = null) : StepInput

    /**
     * [matched] содержит индексы пар (индекс в парах шага). [wrongPair] — индексы пары (слева, справа),
     * которые на мгновение подсвечиваются красным после несовпадения.
     */
    data class Matching(
        val leftSelected: Int? = null,
        val matched: Set<Int> = emptySet(),
        val wrongPair: Pair<Int, Int>? = null,
        val mistakes: Int = 0,
    ) : StepInput

    /** Индексы в банке слов предложения, в порядке, в котором слова были выставлены. */
    data class Sentence(val placed: List<Int> = emptyList()) : StepInput

    data class Translation(val value: TextFieldValue = TextFieldValue()) : StepInput
}

/** Результат проверки шага. Тексты — данные, поэтому передаются обычными строками. */
data class StepFeedback(
    val isCorrect: Boolean,
    /** Показывается при верном ответе, например «китап — книга». */
    val detail: String? = null,
    /** Показывается при неверном ответе. */
    val correctAnswer: String? = null,
    /** Введённый ответ принят после свёртки татарских букв; сюда кладётся правильное написание. */
    val spellingHint: String? = null,
    val xpGained: Int = 0,
)

data class LessonUiState(
    val load: LoadState<Unit> = LoadState.Loading,
    val steps: List<ExerciseStep> = emptyList(),
    val stepIndex: Int = 0,
    val input: StepInput = StepInput.Choice(),
    val feedback: StepFeedback? = null,
    val audio: AudioPlayerState = AudioPlayerState.Idle,
    val showExitDialog: Boolean = false,
) : UiState {
    val step: ExerciseStep? get() = steps.getOrNull(stepIndex)

    /** Прогресс считает шаг пройденным, как только он проверен. */
    val progress: Float
        get() = if (steps.isEmpty()) 0f else (stepIndex + if (feedback != null) 1 else 0).toFloat() / steps.size
}

sealed interface LessonUiEvent : UiEvent {
    data object RetryClicked : LessonUiEvent
    data class OptionSelected(val index: Int) : LessonUiEvent
    data class MatchLeftClicked(val pairIndex: Int) : LessonUiEvent
    data class MatchRightClicked(val pairIndex: Int) : LessonUiEvent
    data class BankWordClicked(val bankIndex: Int) : LessonUiEvent
    data class PlacedWordClicked(val position: Int) : LessonUiEvent
    data class TextChanged(val value: TextFieldValue) : LessonUiEvent
    data class LetterClicked(val letter: String) : LessonUiEvent
    data class PlayAudioClicked(val slow: Boolean = false) : LessonUiEvent
    data object CheckClicked : LessonUiEvent
    data object ContinueClicked : LessonUiEvent
    data object CloseClicked : LessonUiEvent
    data object ExitConfirmed : LessonUiEvent
    data object ExitDismissed : LessonUiEvent
}

sealed interface LessonUiEffect : UiEffect {
    data class NavigateToResult(val key: ResultKey) : LessonUiEffect
    data object Exit : LessonUiEffect
}
