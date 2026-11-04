package ru.tatarcha.tatlearn.feature.profile

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.core.common.AutoDismiss
import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.core.notifications.Reminders
import ru.tatarcha.tatlearn.data.local.HapticsSettings
import ru.tatarcha.tatlearn.data.local.NarratorSettings
import ru.tatarcha.tatlearn.data.local.SoundEffectsSettings
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.ProfileRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository

class ProfileViewModel(
    profile: ProfileRepository,
    progress: ProgressRepository,
    auth: AuthRepository,
    private val haptics: HapticsSettings,
    private val sounds: SoundEffectsSettings,
    private val narrator: NarratorSettings,
    private val reminders: Reminders,
) : MviViewModel<ProfileUiState, ProfileUiEvent, ProfileUiEffect>(ProfileUiState()) {

    private val toastTimer = AutoDismiss(viewModelScope, TamyrMotion.TOAST_VISIBLE_MS)

    init {
        // Пока профиля нет (поток с входом), имя берётся из сессии.
        profile.profile.onEach { current ->
            setState { copy(name = current?.name ?: auth.session.value?.name.orEmpty(), avatar = current?.avatar ?: Avatar.Default) }
        }.launchIn(viewModelScope)
        progress.observeStats().onEach { stats -> setState { copy(stats = stats) } }.launchIn(viewModelScope)
        haptics.enabled.onEach { enabled -> setState { copy(hapticsEnabled = enabled) } }.launchIn(viewModelScope)
        sounds.enabled.onEach { enabled -> setState { copy(soundsEnabled = enabled) } }.launchIn(viewModelScope)
        narrator.voice.onEach { voice -> setState { copy(narratorVoice = voice) } }.launchIn(viewModelScope)
        reminders.enabled.onEach { enabled -> setState { copy(reminderEnabled = enabled) } }.launchIn(viewModelScope)
        reminders.hour.onEach { hour -> setState { copy(reminderHour = hour) } }.launchIn(viewModelScope)
        profile.saved.onEach {
            setState { copy(savedToast = true) }
            toastTimer.start { setState { copy(savedToast = false) } }
        }.launchIn(viewModelScope)
    }

    override fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.EditClicked -> sendEffect(ProfileUiEffect.NavigateToEdit)
            is ProfileUiEvent.HapticsToggled -> haptics.setEnabled(event.enabled)
            is ProfileUiEvent.SoundsToggled -> sounds.setEnabled(event.enabled)
            is ProfileUiEvent.NarratorVoiceSelected -> narrator.setVoice(event.voice)
            is ProfileUiEvent.ReminderToggled -> toggleReminder(event.enabled)
            is ProfileUiEvent.ReminderHourSelected -> viewModelScope.launch { reminders.changeHour(event.hour) }
            ProfileUiEvent.ToastDismissed -> {
                toastTimer.cancel()
                setState { copy(savedToast = false, reminderDeniedToast = false) }
            }
        }
    }

    private fun toggleReminder(enabled: Boolean) {
        if (!enabled) {
            reminders.disable()
            return
        }
        viewModelScope.launch {
            if (!reminders.enable(state.value.reminderHour)) {
                setState { copy(reminderDeniedToast = true) }
                toastTimer.start { setState { copy(reminderDeniedToast = false) } }
            }
        }
    }
}
