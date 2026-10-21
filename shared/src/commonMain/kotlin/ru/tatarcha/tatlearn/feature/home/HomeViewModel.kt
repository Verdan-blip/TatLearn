package ru.tatarcha.tatlearn.feature.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.core.common.errorCode
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.LessonsRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository

class HomeViewModel(
    private val lessons: LessonsRepository,
    private val progress: ProgressRepository,
    auth: AuthRepository,
) : MviViewModel<HomeUiState, HomeUiEvent, HomeUiEffect>(HomeUiState(userName = auth.session.value?.name.orEmpty())) {

    init {
        progress.observeStats().onEach { stats -> setState { copy(stats = stats) } }.launchIn(viewModelScope)
        auth.session.onEach { session -> setState { copy(userName = session?.name.orEmpty()) } }.launchIn(viewModelScope)
        load()
    }

    override fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.RetryClicked -> load()
            HomeUiEvent.StartLessonClicked -> {
                val content = state.value.content
                if (content is LoadState.Content) {
                    sendEffect(HomeUiEffect.NavigateToLesson(content.data.currentLesson.lesson.id))
                }
            }
            // TODO(design): экрана модуля нет в макете.
            is HomeUiEvent.ModuleClicked -> Unit
        }
    }

    private fun load() {
        viewModelScope.launch {
            setState { copy(content = LoadState.Loading) }
            val result = lessons.getHome()
            setState { copy(content = result.fold({ LoadState.Content(it) }, { LoadState.Error(it.errorCode()) })) }
        }
    }
}
