package ru.tatarcha.tatlearn.feature.result

import ru.tatarcha.tatlearn.core.common.UiEffect
import ru.tatarcha.tatlearn.core.common.UiEvent
import ru.tatarcha.tatlearn.core.common.UiState
import ru.tatarcha.tatlearn.domain.model.UserStats

data class ResultUiState(
    val userName: String,
    val xpEarned: Int,
    val accuracyPercent: Int,
    val durationSeconds: Int,
    val mistakes: Int,
    val mistakeStepIds: List<String> = emptyList(),
    /** Урок засчитан: точность не ниже порога или исправлены все ошибки при повторе. */
    val passed: Boolean = true,
    /** Сколько верных ответов не хватило до порога; в повторе ошибок это число неисправленных ошибок. */
    val missingAnswers: Int = 0,
    /** Это повтор ошибок: вместо «нужно ещё верных ответов» показывается число неисправленных ошибок. */
    val isMistakesRun: Boolean = false,
    /** Текущая серия из базы прогресса; 0 скрывает карточку серии. */
    val streak: Int = 0,
    /** XP и уровень после урока из базы; null, пока не загружены (строка уровня скрыта). */
    val stats: UserStats? = null,
    /** Показывать ли «Стрик продлён»: серию продлили именно этим уроком. */
    val streakExtended: Boolean = false,
    /** Не null, пока показано окно с предложением включить напоминания (после первого пройденного урока). */
    val reminderPrompt: ReminderPrompt? = null,
) : UiState

data class ReminderPrompt(val hour: Int)

sealed interface ResultUiEvent : UiEvent {
    data object ContinueClicked : ResultUiEvent
    data object RetryMistakesClicked : ResultUiEvent
    data class ReminderHourSelected(val hour: Int) : ResultUiEvent
    data object ReminderEnableClicked : ResultUiEvent
    data object ReminderLaterClicked : ResultUiEvent
}

sealed interface ResultUiEffect : UiEffect {
    data object NavigateHome : ResultUiEffect
    data class NavigateToMistakes(val lessonId: String, val stepIds: List<String>) : ResultUiEffect
}
