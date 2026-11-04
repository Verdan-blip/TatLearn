package ru.tatarcha.tatlearn.feature.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.core.common.ErrorCodes
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.core.common.errorCode
import ru.tatarcha.tatlearn.domain.HomeContentBuilder
import ru.tatarcha.tatlearn.domain.HomeProgressTracker
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
    private val tracker: HomeProgressTracker,
) : MviViewModel<HomeUiState, HomeUiEvent, HomeUiEffect>(
    HomeUiState(
        userName = profile.profile.value?.name ?: auth.session.value?.name.orEmpty(),
        avatar = profile.profile.value?.avatar ?: Avatar.Default,
    ),
) {

    /** Каталог с сервера и локальный прогресс; главная пересобирается при изменении любого из них. */
    private var catalog: List<CourseModule>? = null
    private var lessonProgress: List<LessonProgress> = emptyList()

    /** Собирать главную можно только после первого ответа базы: пустой прогресс до него дал бы ложный «прирост». */
    private var progressReady = false

    init {
        progress.observeStats().onEach { stats -> setState { copy(stats = stats) } }.launchIn(viewModelScope)
        progress.observeLessonProgress().onEach { items ->
            lessonProgress = items
            progressReady = true
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
            // Вкладки словаря и соревнований это заглушка-экран 16.
            is HomeUiEvent.ModuleClicked -> sendEffect(HomeUiEffect.NavigateToModule(event.moduleId))
            is HomeUiEvent.NavItemClicked -> setState { copy(tab = event.item) }
            HomeUiEvent.ProgressGainStarted -> (state.value.content as? LoadState.Content)?.let { tracker.commit(it.data) }
            HomeUiEvent.ProgressGainFinished -> setState { copy(progressGain = null) }
        }
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
        if (!progressReady) return
        val home = HomeContentBuilder.build(modules, lessonProgress)
        // Прирост запоминаем (commit) только когда экран его показал: старая главная под уроком тоже получает новый прогресс.
        val gain = home?.let(tracker::gainFor)
        if (home != null && gain == null) tracker.commit(home)
        setState {
            copy(
                content = if (home != null) LoadState.Content(home) else LoadState.Error(ErrorCodes.NOT_FOUND),
                progressGain = gain ?: progressGain,
            )
        }
    }
}
