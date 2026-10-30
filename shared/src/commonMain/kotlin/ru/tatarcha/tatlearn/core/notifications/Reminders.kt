package ru.tatarcha.tatlearn.core.notifications

import kotlinx.coroutines.flow.StateFlow
import ru.tatarcha.tatlearn.data.local.ReminderSettings

/**
 * Ежедневное напоминание об уроке: настройки на устройстве плюс платформенные уведомления.
 * [texts] отдаёт текст уведомления из ресурсов (в тестах подменяется).
 */
class Reminders(
    private val platform: PlatformReminders,
    private val settings: ReminderSettings,
    private val texts: suspend () -> ReminderTexts,
) {
    val enabled: StateFlow<Boolean> = settings.enabled
    val hour: StateFlow<Int> = settings.hour

    /** Предлагать ли включить напоминания: ещё не предлагали и они не включены. */
    val shouldOfferPrompt: Boolean get() = !settings.promptShown && !settings.enabled.value

    fun markPromptShown() {
        settings.promptShown = true
    }

    /** Включает напоминание на [hour]; false, если пользователь не дал разрешение (тогда настройки не меняются). */
    suspend fun enable(hour: Int): Boolean {
        if (!platform.requestPermission()) return false
        settings.setHour(hour)
        settings.setEnabled(true)
        platform.schedule(hour, texts())
        return true
    }

    suspend fun changeHour(hour: Int) {
        settings.setHour(hour)
        if (settings.enabled.value) platform.schedule(hour, texts())
    }

    fun disable() {
        settings.setEnabled(false)
        platform.cancel()
    }

    /** Запуск приложения: пересоздаёт расписание (подхватывает смену языка и сбои после обновления системы). */
    suspend fun restore() {
        if (settings.enabled.value) platform.schedule(settings.hour.value, texts())
    }

    companion object {
        /** Часы, из которых выбирает пользователь (кнопки-таблетки). */
        val AvailableHours = listOf(8, 13, 19, 21)
    }
}
