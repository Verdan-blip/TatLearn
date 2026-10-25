package ru.tatarcha.tatlearn.feature.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.core.common.AutoDismiss
import ru.tatarcha.tatlearn.core.common.ErrorCodes
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.core.common.errorCode
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.domain.HomeContentBuilder
import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.repository.LessonsRepository
import ru.tatarcha.tatlearn.domain.repository.ProfileRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository

class HomeViewModel(
    private val lessons: LessonsRepository,
    progress: ProgressRepository,
    auth: AuthRepository,
    profile: ProfileRepository,
) : MviViewModel<HomeUiState, HomeUiEvent, HomeUiEffect>(
    HomeUiState(
        userName = profile.profile.value?.name ?: auth.session.value?.name.orEmpty(),
        avatar = profile.profile.value?.avatar ?: Avatar.Default,
    ),
) {

    private val toastTimer = AutoDismiss(viewModelScope, TamyrMotion.TOAST_VISIBLE_MS)

    /** Каталог с сервера и локальный прогресс; главная пересобирается при изменении любого из них. */
    private var catalog: List<CourseModule>? = null
    private var lessonProgress: List<LessonProgress> = emptyList()

    init {
        progress.observeStats().onEach { stats -> setState { copy(stats = stats) } }.launchIn(viewModelScope)
        progress.observeLessonProgress().onEach { items ->
            lessonProgress = items
            publish()
        }.launchIn(viewModelScope)
        // Имя и аватар из профиля; пока профиля нет (поток с входом), имя берётся из сессии.
        combine(profile.profile, auth.session) { current, session -> current to session }.onEach { (current, session) ->
            setState { copy(userName = current?.name ?: session?.name.orEmpty(), avatar = current?.avatar ?: Avatar.Default) }
        }.launchIn(viewModelScope)
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
            // TODO(design): списка модулей («Все») нет, пока показываем заглушку; вкладки словаря, соревнований и профиля — заглушка-экран 16.
            is HomeUiEvent.ModuleClicked -> sendEffect(HomeUiEffect.NavigateToModule(event.moduleId))
            HomeUiEvent.AllModulesClicked -> showToast(HomeToast.AllModulesUnavailable)
            is HomeUiEvent.NavItemClicked -> setState { copy(tab = event.item, toast = null) }
            HomeUiEvent.ToastDismissed -> hideToast()
        }
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
            catalog = null
            lessons.getModules().fold(
                onSuccess = { modules ->
                    catalog = modules
                    publish()
                },
                onFailure = { error -> setState { copy(content = LoadState.Error(error.errorCode())) } },
            )
        }
    }

    private fun publish() {
        val modules = catalog ?: return
        val home = HomeContentBuilder.build(modules, lessonProgress)
        setState { copy(content = if (home != null) LoadState.Content(home) else LoadState.Error(ErrorCodes.NOT_FOUND)) }
    }
}
