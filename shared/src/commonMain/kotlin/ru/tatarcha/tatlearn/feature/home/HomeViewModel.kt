package ru.tatarcha.tatlearn.feature.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.core.common.AutoDismiss
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.core.common.errorCode
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.LessonsRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository

class HomeViewModel(
    private val lessons: LessonsRepository,
    progress: ProgressRepository,
    auth: AuthRepository,
) : MviViewModel<HomeUiState, HomeUiEvent, HomeUiEffect>(HomeUiState(userName = auth.session.value?.name.orEmpty())) {

    private val toastTimer = AutoDismiss(viewModelScope, TamyrMotion.TOAST_VISIBLE_MS)

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
            // TODO(design): экранов модуля и списка модулей нет, пока показываем заглушку; вкладки словаря, соревнований и профиля — заглушка-экран 16.
            is HomeUiEvent.ModuleClicked -> onModuleClicked(event.moduleId)
            HomeUiEvent.AllModulesClicked -> showToast(HomeToast.AllModulesUnavailable)
            is HomeUiEvent.NavItemClicked -> setState { copy(tab = event.item, toast = null) }
            HomeUiEvent.ToastDismissed -> hideToast()
        }
    }

    private fun onModuleClicked(moduleId: String) {
        val content = state.value.content as? LoadState.Content ?: return
        val module = content.data.modules.firstOrNull { it.id == moduleId } ?: return
        showToast(if (module.isLocked) HomeToast.ModuleLocked(module.lockHint) else HomeToast.ModuleUnavailable)
    }

    private fun showToast(toast: HomeToast) {
        setState { copy(toast = toast) }
        toastTimer.start { setState { copy(toast = null) } }
    }

    private fun hideToast() {
        toastTimer.cancel()
        setState { copy(toast = null) }
    }

    private fun load() {
        viewModelScope.launch {
            setState { copy(content = LoadState.Loading) }
            val result = lessons.getHome()
            setState { copy(content = result.fold({ LoadState.Content(it) }, { LoadState.Error(it.errorCode()) })) }
        }
    }
}
