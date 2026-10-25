package ru.tatarcha.tatlearn.feature.module

import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.common.UiEffect
import ru.tatarcha.tatlearn.core.common.UiEvent
import ru.tatarcha.tatlearn.core.common.UiState
import ru.tatarcha.tatlearn.domain.model.ModuleDetails
import ru.tatarcha.tatlearn.domain.model.RequiredModule

data class ModuleUiState(
    val content: LoadState<ModuleDetails> = LoadState.Loading,
    /** Лист «Урок пока закрыт»: показывается, пока не null; внутри модуль, который нужно пройти. */
    val lockedSheet: RequiredModule? = null,
) : UiState

sealed interface ModuleUiEvent : UiEvent {
    data object RetryClicked : ModuleUiEvent
    data object BackClicked : ModuleUiEvent
    data class LessonClicked(val lessonId: String) : ModuleUiEvent
    data object ContinueClicked : ModuleUiEvent
    data object NextModuleClicked : ModuleUiEvent
    data object LockedSheetConfirmed : ModuleUiEvent
    data object LockedSheetDismissed : ModuleUiEvent
}

sealed interface ModuleUiEffect : UiEffect {
    data object NavigateBack : ModuleUiEffect
    data class NavigateToLesson(val lessonId: String) : ModuleUiEffect
    data class NavigateToModule(val moduleId: String) : ModuleUiEffect
}
