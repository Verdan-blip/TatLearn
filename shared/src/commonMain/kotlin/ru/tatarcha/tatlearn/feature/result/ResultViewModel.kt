package ru.tatarcha.tatlearn.feature.result

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.core.haptics.Haptics
import ru.tatarcha.tatlearn.core.notifications.Reminders
import ru.tatarcha.tatlearn.domain.exercise.PassRules
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.ProfileRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository
import ru.tatarcha.tatlearn.navigation.ResultKey

class ResultViewModel(
    private val key: ResultKey,
    auth: AuthRepository,
    progress: ProgressRepository,
    profile: ProfileRepository,
    haptics: Haptics,
    private val reminders: Reminders,
) : MviViewModel<ResultUiState, ResultUiEvent, ResultUiEffect>(
    ResultUiState(
        userName = profile.profile.value?.name ?: auth.session.value?.name.orEmpty(),
        xpEarned = key.xpEarned,
        accuracyPercent = key.accuracyPercent,
        durationSeconds = key.durationSeconds,
        mistakes = key.mistakeStepIds.size,
        mistakeStepIds = key.mistakeStepIds,
        passed = PassRules.isPassed(key.accuracyPercent, key.isMistakesRun, key.mistakeStepIds.size),
        isMistakesRun = key.isMistakesRun,
        streakExtended = key.streakExtended,
        missingAnswers = if (key.isMistakesRun) key.mistakeStepIds.size else PassRules.missingAnswers(key.correctAnswers, key.totalAnswers),
    ),
) {
    init {
        // Праздничный отклик один раз при показе пройденного урока; «не засчитан» без вибро (тон «хорошая попытка»).
        if (state.value.passed) haptics.celebrate()
        // Предложение напоминаний только один раз и только после засчитанного урока: к этому моменту у человека уже есть польза.
        if (state.value.passed && reminders.shouldOfferPrompt) {
            reminders.markPromptShown()
            setState { copy(reminderPrompt = ReminderPrompt(hour = reminders.hour.value)) }
        }
        progress.observeStats().onEach { stats -> setState { copy(streak = stats.streak) } }.launchIn(viewModelScope)
    }

    override fun onEvent(event: ResultUiEvent) {
        when (event) {
            ResultUiEvent.ContinueClicked -> sendEffect(ResultUiEffect.NavigateHome)
            ResultUiEvent.RetryMistakesClicked -> sendEffect(ResultUiEffect.NavigateToMistakes(key.lessonId, key.mistakeStepIds))
            is ResultUiEvent.ReminderHourSelected -> setState { copy(reminderPrompt = reminderPrompt?.copy(hour = event.hour)) }
            ResultUiEvent.ReminderEnableClicked -> {
                val hour = state.value.reminderPrompt?.hour ?: return
                setState { copy(reminderPrompt = null) }
                viewModelScope.launch { reminders.enable(hour) }
            }
            ResultUiEvent.ReminderLaterClicked -> setState { copy(reminderPrompt = null) }
        }
    }
}
