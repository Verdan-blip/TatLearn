package ru.tatarcha.tatlearn.feature.home

import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.common.UiEffect
import ru.tatarcha.tatlearn.core.common.UiEvent
import ru.tatarcha.tatlearn.core.common.UiState
import ru.tatarcha.tatlearn.domain.model.HomeContent
import ru.tatarcha.tatlearn.domain.model.UserStats

data class HomeUiState(
    val userName: String = "",
    /** Серия и XP приходят из локальной базы, независимо от запроса главной. */
    val stats: UserStats = UserStats.Empty,
    val content: LoadState<HomeContent> = LoadState.Loading,
    val toast: HomeToast? = null,
) : UiState

sealed interface HomeUiEvent : UiEvent {
    data object RetryClicked : HomeUiEvent
    data object StartLessonClicked : HomeUiEvent
    data class ModuleClicked(val moduleId: String) : HomeUiEvent
    data object AllModulesClicked : HomeUiEvent
    data class NavItemClicked(val item: HomeNavItem) : HomeUiEvent
    data object ToastDismissed : HomeUiEvent
}

sealed interface HomeUiEffect : UiEffect {
    data class NavigateToLesson(val lessonId: String) : HomeUiEffect
}
