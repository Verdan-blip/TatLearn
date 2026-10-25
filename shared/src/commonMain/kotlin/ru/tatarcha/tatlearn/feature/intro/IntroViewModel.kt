package ru.tatarcha.tatlearn.feature.intro

import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.ProfileRules
import ru.tatarcha.tatlearn.domain.repository.ProfileRepository

class IntroViewModel(
    private val profile: ProfileRepository,
) : MviViewModel<IntroUiState, IntroUiEvent, IntroUiEffect>(IntroUiState()) {

    override fun onEvent(event: IntroUiEvent) {
        when (event) {
            is IntroUiEvent.NameChanged -> setState { copy(name = event.name) }
            is IntroUiEvent.ColorSelected -> (event.avatar as? Avatar.Color)?.let { color -> setState { copy(avatar = color) } }
            IntroUiEvent.ContinueClicked -> onContinue()
            IntroUiEvent.SkipClicked -> finish(ProfileRules.DEFAULT_NAME, Avatar.Default)
        }
    }

    private fun onContinue() {
        val current = state.value
        if (!current.canContinue) return
        finish(ProfileRules.normalize(current.name).ifEmpty { ProfileRules.DEFAULT_NAME }, current.avatar)
    }

    private fun finish(name: String, avatar: Avatar) {
        profile.save(name, avatar)
        sendEffect(IntroUiEffect.NavigateHome)
    }
}
