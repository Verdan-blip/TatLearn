package ru.tatarcha.tatlearn.feature.result

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.domain.exercise.PassRules
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository
import ru.tatarcha.tatlearn.navigation.ResultKey

class ResultViewModel(
    private val key: ResultKey,
    auth: AuthRepository,
    progress: ProgressRepository,
) : MviViewModel<ResultUiState, ResultUiEvent, ResultUiEffect>(
    ResultUiState(
        userName = auth.session.value?.name.orEmpty(),
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
        progress.observeStats().onEach { stats -> setState { copy(streak = stats.streak) } }.launchIn(viewModelScope)
    }

    override fun onEvent(event: ResultUiEvent) {
        when (event) {
            ResultUiEvent.ContinueClicked -> sendEffect(ResultUiEffect.NavigateHome)
            ResultUiEvent.RetryMistakesClicked -> sendEffect(ResultUiEffect.NavigateToMistakes(key.lessonId, key.mistakeStepIds))
        }
    }
}
