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
import ru.tatarcha.tatlearn.domain.repository.LessonsRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository

/** Только для отладки: прогоняет слой данных из UI kit (фейковый бэкенд, база прогресса). */
data class ShowcaseUiState(
    val stats: UserStats = UserStats.Empty,
    val home: LoadState<List<CourseModule>> = LoadState.Loading,
    val simulateErrors: Boolean = false,
) : UiState

sealed interface ShowcaseUiEvent : UiEvent {
    data object LoadHome : ShowcaseUiEvent
    data object ToggleSimulateErrors : ShowcaseUiEvent
    data object RecordResult : ShowcaseUiEvent
}

data object ShowcaseUiEffect : UiEffect

class ShowcaseViewModel(
    private val lessons: LessonsRepository,
    private val progress: ProgressRepository,
    private val fakeConfig: FakeBackendConfig,
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
        }
    }
}
