package ru.tatarcha.tatlearn.core.notifications

/**
 * Платформенные локальные уведомления (на сервере ничего не нужно). Реализации: Android (WorkManager),
 * iOS (UNUserNotificationCenter).
 */
interface PlatformReminders {
    /** Просит у системы разрешение показывать уведомления; true, если оно есть (в том числе выдано раньше). */
    suspend fun requestPermission(): Boolean

    /** Ежедневное уведомление в [hour]:00 по местному времени; заменяет ранее запланированное. */
    fun schedule(hour: Int, texts: ReminderTexts)

    fun cancel()

    /** Отладка: показать уведомление прямо сейчас (на iOS через несколько секунд: на переднем плане iOS его не показывает). */
    fun showNow(texts: ReminderTexts)
}

/** [channelName] нужен только Android (название канала уведомлений в системных настройках). */
data class ReminderTexts(val title: String, val body: String, val channelName: String)
