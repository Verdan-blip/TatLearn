package ru.tatarcha.tatlearn.feature.onboarding

import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.data.local.OnboardingStorage
import ru.tatarcha.tatlearn.feature.auth.AuthMode

class OnboardingViewModel(
    private val storage: OnboardingStorage,
) : MviViewModel<OnboardingUiState, OnboardingUiEvent, OnboardingUiEffect>(OnboardingUiState()) {

    override fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            is OnboardingUiEvent.PageChanged -> setState { copy(currentPage = event.page) }
            OnboardingUiEvent.NextClicked -> onNext()
            OnboardingUiEvent.BackClicked -> setState { copy(currentPage = (currentPage - 1).coerceAtLeast(0)) }
            OnboardingUiEvent.SkipClicked -> finish(AuthMode.SignIn)
        }
    }

    private fun onNext() {
        if (state.value.isLastPage) {
            finish(AuthMode.SignUp)
        } else {
            setState { copy(currentPage = currentPage + 1) }
        }
    }

    private fun finish(mode: AuthMode) {
        storage.markSeen()
        sendEffect(OnboardingUiEffect.NavigateToAuth(mode))
    }
}
