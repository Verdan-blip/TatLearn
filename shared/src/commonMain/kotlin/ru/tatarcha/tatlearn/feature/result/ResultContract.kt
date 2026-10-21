package ru.tatarcha.tatlearn.feature.result

import ru.tatarcha.tatlearn.core.common.UiEffect
import ru.tatarcha.tatlearn.core.common.UiEvent
import ru.tatarcha.tatlearn.core.common.UiState

data class ResultUiState(
    val userName: String,
    val xpEarned: Int,
    val accuracyPercent: Int,
    val durationSeconds: Int,
    val mistakes: Int,
    val mistakeStepIds: List<String> = emptyList(),
    /** Текущая серия из базы прогресса; 0 скрывает карточку серии. */
    val streak: Int = 0,
) : UiState

sealed interface ResultUiEvent : UiEvent {
    data object ContinueClicked : ResultUiEvent
    data object RetryMistakesClicked : ResultUiEvent
}

sealed interface ResultUiEffect : UiEffect {
    data object NavigateHome : ResultUiEffect
    data class NavigateToMistakes(val lessonId: String, val stepIds: List<String>) : ResultUiEffect
}
