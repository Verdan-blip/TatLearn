package ru.tatarcha.tatlearn.core.notifications

import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * expect/actual (сторона iOS): локальное уведомление `UNCalendarNotificationTrigger` с повтором раз в день в нужный час;
 * запрос разрешения показывает системный диалог один раз, при отказе повторный запрос молча возвращает false
 * (включить можно только в настройках iPhone). Проверка: в симуляторе включить напоминание в профиле на ближайший час,
 * свернуть приложение (на переднем плане iOS уведомление не показывает) и дождаться баннера.
 */
class IosReminders : PlatformReminders {
    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun requestPermission(): Boolean = suspendCoroutine { continuation ->
        center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { granted, _ ->
            continuation.resume(granted)
        }
    }

    override fun schedule(hour: Int, texts: ReminderTexts) {
        val time = NSDateComponents().apply {
            setHour(hour.toLong())
            setMinute(0)
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(time, repeats = true)
        // Тот же идентификатор заменяет прежнее расписание.
        center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(REQUEST_ID, content(texts), trigger), withCompletionHandler = null)
    }

    override fun showNow(texts: ReminderTexts) {
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(DEBUG_DELAY_SECONDS, repeats = false)
        center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(DEBUG_REQUEST_ID, content(texts), trigger), withCompletionHandler = null)
    }

    override fun cancel() {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(REQUEST_ID))
    }

    private fun content(texts: ReminderTexts) = UNMutableNotificationContent().apply {
        setTitle(texts.title)
        setBody(texts.body)
        setSound(UNNotificationSound.defaultSound)
    }

    private companion object {
        const val REQUEST_ID = "daily_reminder"
        const val DEBUG_REQUEST_ID = "debug_reminder"
        const val DEBUG_DELAY_SECONDS = 5.0
    }
}
