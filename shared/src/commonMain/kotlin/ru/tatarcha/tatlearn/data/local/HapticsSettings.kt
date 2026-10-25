package ru.tatarcha.tatlearn.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Настройка «Вибро-отклик» из профиля; по умолчанию включена (решение владельца 25.10.2026). */
class HapticsSettings(private val settings: Settings) {
    private val _enabled = MutableStateFlow(settings.getBoolean(KEY_ENABLED, true))
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    fun setEnabled(value: Boolean) {
        settings.putBoolean(KEY_ENABLED, value)
        _enabled.value = value
    }

    private companion object {
        const val KEY_ENABLED = "haptics_enabled"
    }
}
