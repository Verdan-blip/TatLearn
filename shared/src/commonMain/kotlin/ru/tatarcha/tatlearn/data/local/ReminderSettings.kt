package ru.tatarcha.tatlearn.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Настройки ежедневного напоминания; по умолчанию выключено, время 19:00 (решение владельца 30.10.2026). */
class ReminderSettings(private val settings: Settings) {
    private val _enabled = MutableStateFlow(settings.getBoolean(KEY_ENABLED, false))
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _hour = MutableStateFlow(settings.getInt(KEY_HOUR, DEFAULT_HOUR))
    val hour: StateFlow<Int> = _hour.asStateFlow()

    /** Окно с предложением напоминаний после первого пройденного урока уже показывали. */
    var promptShown: Boolean
        get() = settings.getBoolean(KEY_PROMPT_SHOWN, false)
        set(value) = settings.putBoolean(KEY_PROMPT_SHOWN, value)

    fun setEnabled(value: Boolean) {
        settings.putBoolean(KEY_ENABLED, value)
        _enabled.value = value
    }

    fun setHour(value: Int) {
        settings.putInt(KEY_HOUR, value)
        _hour.value = value
    }

    companion object {
        const val DEFAULT_HOUR = 19
        private const val KEY_ENABLED = "reminder_enabled"
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_PROMPT_SHOWN = "reminder_prompt_shown"
    }
}
