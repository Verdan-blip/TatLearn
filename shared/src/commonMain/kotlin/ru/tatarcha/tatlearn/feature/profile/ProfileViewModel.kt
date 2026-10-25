package ru.tatarcha.tatlearn.feature.profile

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.tatarcha.tatlearn.core.common.AutoDismiss
import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.ProfileRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository

class ProfileViewModel(
    profile: ProfileRepository,
    progress: ProgressRepository,
    auth: AuthRepository,
) : MviViewModel<ProfileUiState, ProfileUiEvent, ProfileUiEffect>(ProfileUiState()) {

    private val toastTimer = AutoDismiss(viewModelScope, TamyrMotion.TOAST_VISIBLE_MS)

    init {
        // Пока профиля нет (поток с входом), имя берётся из сессии.
        profile.profile.onEach { current ->
            setState { copy(name = current?.name ?: auth.session.value?.name.orEmpty(), avatar = current?.avatar ?: Avatar.Default) }
        }.launchIn(viewModelScope)
        progress.observeStats().onEach { stats -> setState { copy(stats = stats) } }.launchIn(viewModelScope)
        profile.saved.onEach {
            setState { copy(savedToast = true) }
            toastTimer.start { setState { copy(savedToast = false) } }
        }.launchIn(viewModelScope)
    }

    override fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.EditClicked -> sendEffect(ProfileUiEffect.NavigateToEdit)
            ProfileUiEvent.ToastDismissed -> {
                toastTimer.cancel()
                setState { copy(savedToast = false) }
            }
        }
    }
}
