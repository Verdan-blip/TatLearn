package ru.tatarcha.tatlearn.feature.profile

import ru.tatarcha.tatlearn.core.common.UiEffect
import ru.tatarcha.tatlearn.core.common.UiEvent
import ru.tatarcha.tatlearn.core.common.UiState
import ru.tatarcha.tatlearn.data.local.ReminderSettings
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.UserStats

/** Вкладка «Профиль» (макеты 26 и 33): имя, аватар, серия, XP, уровень. */
data class ProfileUiState(
    val name: String = "",
    val avatar: Avatar = Avatar.Default,
    val stats: UserStats = UserStats.Empty,
    /** Показывать уведомление «Сохранено» после возврата с экрана изменения. */
    val savedToast: Boolean = false,
    /** Настройка «Вибро-отклик» (по умолчанию включена). */
    val hapticsEnabled: Boolean = true,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = ReminderSettings.DEFAULT_HOUR,
    /** Показать уведомление «Разрешите уведомления в настройках»: пользователь отказал в системном разрешении. */
    val reminderDeniedToast: Boolean = false,
) : UiState

sealed interface ProfileUiEvent : UiEvent {
    data object EditClicked : ProfileUiEvent
    data object ToastDismissed : ProfileUiEvent
    data class HapticsToggled(val enabled: Boolean) : ProfileUiEvent
    data class ReminderToggled(val enabled: Boolean) : ProfileUiEvent
    data class ReminderHourSelected(val hour: Int) : ProfileUiEvent
}

sealed interface ProfileUiEffect : UiEffect {
    data object NavigateToEdit : ProfileUiEffect
}
