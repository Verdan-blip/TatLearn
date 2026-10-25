package ru.tatarcha.tatlearn.feature.showcase

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.core.common.UiEffect
import ru.tatarcha.tatlearn.core.common.UiEvent
import ru.tatarcha.tatlearn.core.common.UiState
import ru.tatarcha.tatlearn.core.common.errorCode
import ru.tatarcha.tatlearn.data.fake.FakeBackendConfig
import ru.tatarcha.tatlearn.domain.model.ExerciseResult
import ru.tatarcha.tatlearn.domain.model.ExerciseType
import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.UserStats
import ru.tatarcha.tatlearn.data.local.OnboardingStorage
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.LessonsRepository
import ru.tatarcha.tatlearn.domain.repository.ProfileRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository

/** Только для отладки: прогоняет слой данных из UI kit (фейковый бэкенд, база прогресса). */
/** Отладочные действия над данными приложения; необратимые подтверждаются листом. */
enum class DebugAction { ResetLessons, ResetStats, SignOut, ResetOnboarding }

data class ShowcaseUiState(
    val stats: UserStats = UserStats.Empty,
    val home: LoadState<List<CourseModule>> = LoadState.Loading,
    val simulateErrors: Boolean = false,
    /** Действие, ожидающее подтверждения. */
    val pendingAction: DebugAction? = null,
) : UiState

sealed interface ShowcaseUiEvent : UiEvent {
    data object LoadHome : ShowcaseUiEvent
    data object ToggleSimulateErrors : ShowcaseUiEvent
    data object RecordResult : ShowcaseUiEvent
    data class ActionRequested(val action: DebugAction) : ShowcaseUiEvent
    data object ActionConfirmed : ShowcaseUiEvent
    data object ActionCancelled : ShowcaseUiEvent
}

sealed interface ShowcaseUiEffect : UiEffect {
    /** После выхода из аккаунта: стек навигации заменяется экраном входа. */
    data object NavigateToAuth : ShowcaseUiEffect

    /** После сброса онбординга: стек заменяется онбордингом. */
    data object NavigateToOnboarding : ShowcaseUiEffect
}

class ShowcaseViewModel(
    private val lessons: LessonsRepository,
    private val progress: ProgressRepository,
    private val fakeConfig: FakeBackendConfig,
    private val auth: AuthRepository,
    private val onboarding: OnboardingStorage,
    private val profile: ProfileRepository,
) : MviViewModel<ShowcaseUiState, ShowcaseUiEvent, ShowcaseUiEffect>(ShowcaseUiState()) {

    init {
        progress.observeStats().onEach { stats -> setState { copy(stats = stats) } }.launchIn(viewModelScope)
        fakeConfig.simulateErrors.onEach { on -> setState { copy(simulateErrors = on) } }.launchIn(viewModelScope)
        onEvent(ShowcaseUiEvent.LoadHome)
    }

    override fun onEvent(event: ShowcaseUiEvent) {
        when (event) {
            ShowcaseUiEvent.LoadHome -> viewModelScope.launch {
                setState { copy(home = LoadState.Loading) }
                val result = lessons.getModules()
                setState {
                    copy(home = result.fold({ LoadState.Content(it) }, { LoadState.Error(it.errorCode()) }))
                }
            }
            ShowcaseUiEvent.ToggleSimulateErrors -> fakeConfig.setSimulateErrors(!state.value.simulateErrors)
            ShowcaseUiEvent.RecordResult -> viewModelScope.launch {
                progress.recordExerciseResult(ExerciseResult("lesson_4", ExerciseType.Quiz, correct = 3, total = 4))
            }
            is ShowcaseUiEvent.ActionRequested -> setState { copy(pendingAction = event.action) }
            ShowcaseUiEvent.ActionCancelled -> setState { copy(pendingAction = null) }
            ShowcaseUiEvent.ActionConfirmed -> confirmAction()
        }
    }

    private fun confirmAction() {
        val action = state.value.pendingAction ?: return
        setState { copy(pendingAction = null) }
        viewModelScope.launch {
            when (action) {
                DebugAction.ResetLessons -> progress.resetLessonProgress()
                DebugAction.ResetStats -> progress.resetStats()
                DebugAction.SignOut -> {
                    auth.signOut()
                    sendEffect(ShowcaseUiEffect.NavigateToAuth)
                }
                DebugAction.ResetOnboarding -> {
                    auth.signOut()
                    onboarding.reset()
                    profile.clear()
                    sendEffect(ShowcaseUiEffect.NavigateToOnboarding)
                }
            }
        }
    }
}
