package ru.tatarcha.tatlearn.feature.onboarding

import ru.tatarcha.tatlearn.core.common.UiEffect
import ru.tatarcha.tatlearn.core.common.UiEvent
import ru.tatarcha.tatlearn.core.common.UiState
import ru.tatarcha.tatlearn.feature.auth.AuthMode

data class OnboardingUiState(
    val pages: List<OnboardingPage> = DefaultOnboardingPages,
    val currentPage: Int = 0,
) : UiState {
    val canGoBack: Boolean get() = currentPage > 0
    val isLastPage: Boolean get() = currentPage == pages.lastIndex
}

sealed interface OnboardingUiEvent : UiEvent {
    data class PageChanged(val page: Int) : OnboardingUiEvent
    data object NextClicked : OnboardingUiEvent
    data object BackClicked : OnboardingUiEvent
    data object SkipClicked : OnboardingUiEvent
}

sealed interface OnboardingUiEffect : UiEffect {
    data class NavigateToAuth(val mode: AuthMode) : OnboardingUiEffect
}
