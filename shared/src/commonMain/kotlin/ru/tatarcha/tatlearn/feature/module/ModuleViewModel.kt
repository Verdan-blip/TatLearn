package ru.tatarcha.tatlearn.feature.module

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.core.common.ErrorCodes
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.core.common.errorCode
import ru.tatarcha.tatlearn.domain.ModuleDetailsBuilder
import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.model.LessonRowState
import ru.tatarcha.tatlearn.domain.repository.LessonsRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository

class ModuleViewModel(
    private val moduleId: String,
    private val lessons: LessonsRepository,
    progress: ProgressRepository,
) : MviViewModel<ModuleUiState, ModuleUiEvent, ModuleUiEffect>(ModuleUiState()) {

    /** Каталог с сервера и локальный прогресс; экран пересобирается при изменении любого из них. */
    private var catalog: List<CourseModule>? = null
    private var lessonProgress: List<LessonProgress> = emptyList()

    init {
        progress.observeLessonProgress().onEach { items ->
            lessonProgress = items
            publish()
        }.launchIn(viewModelScope)
        load()
    }

    override fun onEvent(event: ModuleUiEvent) {
        when (event) {
            ModuleUiEvent.RetryClicked -> load()
            ModuleUiEvent.BackClicked -> sendEffect(ModuleUiEffect.NavigateBack)
            is ModuleUiEvent.LessonClicked -> onLessonClicked(event.lessonId)
            ModuleUiEvent.ContinueClicked -> details()?.nextLesson?.let { sendEffect(ModuleUiEffect.NavigateToLesson(it.id)) }
            ModuleUiEvent.NextModuleClicked -> details()?.nextModule?.let { sendEffect(ModuleUiEffect.NavigateToModule(it.id)) }
            ModuleUiEvent.LockedSheetConfirmed -> {
                val required = state.value.lockedSheet ?: return
                setState { copy(lockedSheet = null) }
                sendEffect(ModuleUiEffect.NavigateToModule(required.id))
            }
            ModuleUiEvent.LockedSheetDismissed -> setState { copy(lockedSheet = null) }
        }
    }

    private fun onLessonClicked(lessonId: String) {
        val details = details() ?: return
        val row = details.lessons.firstOrNull { it.lesson.id == lessonId } ?: return
        if (row.state == LessonRowState.Locked) {
            setState { copy(lockedSheet = details.lockedBy) }
        } else {
            sendEffect(ModuleUiEffect.NavigateToLesson(lessonId))
        }
    }

    private fun details() = (state.value.content as? LoadState.Content)?.data

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
        val details = ModuleDetailsBuilder.build(moduleId, modules, lessonProgress)
        setState {
            copy(content = if (details != null) LoadState.Content(details) else LoadState.Error(ErrorCodes.NOT_FOUND))
        }
    }
}
