package ru.tatarcha.tatlearn.core.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * expect/actual (сторона Android): ежедневное напоминание через WorkManager. Каждый запуск показывает уведомление
 * и ставит следующий на завтра в то же время; WorkManager переживает перезагрузку телефона. Время приблизительное
 * (система может сдвинуть на минуты в режиме экономии). Разрешение POST_NOTIFICATIONS нужно с Android 13.
 * Проверка: включить напоминание в профиле, поставить ближайший час (или сменить время телефона) и дождаться уведомления.
 */
class AndroidReminders(private val context: Context) : PlatformReminders {

    override suspend fun requestPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        return granted || NotificationPermissionRequester.request()
    }

    override fun schedule(hour: Int, texts: ReminderTexts) = enqueueReminder(context, hour, texts)

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }
}

internal const val REMINDER_WORK_NAME = "daily_reminder"
internal const val KEY_HOUR = "hour"
internal const val KEY_TITLE = "title"
internal const val KEY_BODY = "body"
internal const val KEY_CHANNEL = "channel"

internal fun enqueueReminder(context: Context, hour: Int, texts: ReminderTexts) {
    val request = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInitialDelay(millisUntilNext(hour), TimeUnit.MILLISECONDS)
        .setInputData(workDataOf(KEY_HOUR to hour, KEY_TITLE to texts.title, KEY_BODY to texts.body, KEY_CHANNEL to texts.channelName))
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork(REMINDER_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
}

/** Миллисекунды до ближайших [hour]:00 по местному времени (если сегодня уже прошло, то до завтрашних). */
internal fun millisUntilNext(hour: Int, now: Calendar = Calendar.getInstance()): Long {
    val next = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
    }
    return next.timeInMillis - now.timeInMillis
}
