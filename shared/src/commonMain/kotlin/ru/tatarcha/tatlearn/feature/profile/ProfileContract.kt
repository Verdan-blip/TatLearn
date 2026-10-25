package ru.tatarcha.tatlearn.feature.profile

import ru.tatarcha.tatlearn.core.common.UiEffect
import ru.tatarcha.tatlearn.core.common.UiEvent
import ru.tatarcha.tatlearn.core.common.UiState
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.UserStats

/** Вкладка «Профиль» (макеты 26 и 33): имя, аватар, серия, XP, уровень. */
data class ProfileUiState(
    val name: String = "",
    val avatar: Avatar = Avatar.Default,
    val stats: UserStats = UserStats.Empty,
    /** Показывать уведомление «Сохранено» после возврата с экрана изменения. */
    val savedToast: Boolean = false,
) : UiState

sealed interface ProfileUiEvent : UiEvent {
    data object EditClicked : ProfileUiEvent
    data object ToastDismissed : ProfileUiEvent
}

sealed interface ProfileUiEffect : UiEffect {
    data object NavigateToEdit : ProfileUiEffect
}
