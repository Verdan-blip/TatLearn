package ru.tatarcha.tatlearn.feature.profile

import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.ProfileRules
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.ProfileRepository

class ProfileEditViewModel(
    private val profile: ProfileRepository,
    auth: AuthRepository,
) : MviViewModel<ProfileEditUiState, ProfileEditUiEvent, ProfileEditUiEffect>(initialState(profile, auth)) {

    override fun onEvent(event: ProfileEditUiEvent) {
        when (event) {
            is ProfileEditUiEvent.NameChanged -> setState { copy(name = event.name) }
            is ProfileEditUiEvent.AvatarSelected -> setState { copy(avatar = event.avatar) }
            ProfileEditUiEvent.SaveClicked -> save()
            ProfileEditUiEvent.BackClicked ->
                if (state.value.hasChanges) setState { copy(exitSheet = true) } else sendEffect(ProfileEditUiEffect.Close)
            ProfileEditUiEvent.ExitConfirmed -> {
                setState { copy(exitSheet = false) }
                sendEffect(ProfileEditUiEffect.Close)
            }
            ProfileEditUiEvent.ExitDismissed -> setState { copy(exitSheet = false) }
        }
    }

    private fun save() {
        val current = state.value
        if (!current.canSave) return
        profile.save(ProfileRules.normalize(current.name), current.avatar)
        sendEffect(ProfileEditUiEffect.Close)
    }

    private companion object {
        fun initialState(profile: ProfileRepository, auth: AuthRepository): ProfileEditUiState {
            val current = profile.profile.value
            val name = current?.name ?: auth.session.value?.name.orEmpty()
            val avatar = current?.avatar ?: Avatar.Default
            return ProfileEditUiState(name = name, avatar = avatar, initialName = name, initialAvatar = avatar)
        }
    }
}
